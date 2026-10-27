package com.surrealdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Person;

/**
 * Regression tests for native handle lifetime. Cleanup used to run from
 * finalize() on a non-volatile pointer: a close() and the finalizer could both
 * free it, and a temporary ValueMut whose pointer had already been handed to a
 * native call could be freed while that call was still running. Debug builds of
 * the native library track every allocation, so a use-after-free surfaces as a
 * SurrealException ("Invalid pointer") instead of corrupting memory.
 */
public class NativeLifecycleTests {

	@Test
	void closeIsIdempotentAndUseAfterCloseFailsCleanly() {
		final Surreal surreal = new Surreal();
		surreal.connect("memory").useNs("test_ns").useDb("test_db");
		surreal.close();
		assertDoesNotThrow(surreal::close);
		// A released handle reads as a null pointer, which the native layer reports
		// as a NullPointerException instead of dereferencing freed memory.
		assertThrows(NullPointerException.class, () -> surreal.query("RETURN 1"));
	}

	@Test
	void concurrentCloseFreesOnce() throws InterruptedException {
		for (int round = 0; round < 50; round++) {
			final Surreal surreal = new Surreal();
			final CountDownLatch start = new CountDownLatch(1);
			final AtomicReference<Throwable> failure = new AtomicReference<>();
			final List<Thread> threads = new ArrayList<>();
			for (int t = 0; t < 4; t++) {
				final Thread thread = new Thread(() -> {
					try {
						start.await();
						surreal.close();
					} catch (Throwable e) {
						failure.compareAndSet(null, e);
					}
				});
				thread.start();
				threads.add(thread);
			}
			start.countDown();
			for (final Thread thread : threads) {
				thread.join();
			}
			assertNull(failure.get());
		}
	}

	@Test
	void temporariesSurviveGarbageCollectionDuringNativeCalls() throws InterruptedException {
		final AtomicBoolean running = new AtomicBoolean(true);
		final Thread collector = new Thread(() -> {
			while (running.get()) {
				System.gc();
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		collector.setDaemon(true);
		collector.start();
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Person[] people = new Person[16];
			for (int i = 0; i < people.length; i++) {
				people[i] = new Person("p" + i, Arrays.asList("a", "b"), i, true, null);
			}
			for (int round = 0; round < 20; round++) {
				// Every call converts its arguments to temporary ValueMuts whose only
				// reference is local to the call.
				assertEquals(people.length, surreal.insert("person", people).size());
				surreal.query("RETURN $a", java.util.Collections.singletonMap("a", Arrays.asList(1, 2, 3)));
				surreal.upsert(new RecordId("person", "x" + round), UpType.CONTENT, people[0]);
			}
		} finally {
			running.set(false);
			collector.join();
		}
	}

	@Test
	void unreferencedOwnersSurviveGarbageCollectionDuringNativeCalls() throws InterruptedException {
		final AtomicBoolean running = new AtomicBoolean(true);
		final Thread collector = new Thread(() -> {
			while (running.get()) {
				System.gc();
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					return;
				}
			}
		});
		collector.setDaemon(true);
		collector.start();
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("CREATE person:1 SET name = 'a'");
			final Value value = surreal.query("RETURN array::range(0, 100)").take(0);
			// Each chain leaves a wrapper whose only use is the getPtr() passed to a
			// native call: the Array, the iterator and the RecordIds below are
			// unreachable from Java while the call runs. The loop runs long enough for
			// the JIT to compile it, which is when such a wrapper can be collected
			// mid-call. This is a smoke test: the race window is a few instructions
			// wide, so it is not expected to catch a regression reliably. Receivers are
			// kept alive by being instance natives, and other arguments by fences.
			for (int i = 0; i < 10_000; i++) {
				assertEquals(0L, value.getArray().iterator().next().getLong());
				assertEquals(100, value.getArray().len());
				if (i % 100 == 0) {
					assertEquals(true, surreal.select(new RecordId("person", 1L)).isPresent());
					ValueMut.createRecordId(new RecordId("person", Array.of(1L, "a")));
				}
			}
		} finally {
			running.set(false);
			collector.join();
		}
	}

	@Test
	void failedEntryCreationStillConsumesTheValue() {
		final ValueMut value = ValueMut.createString("x");
		// A null name fails in the native call. The value was handed over, so it must
		// have been consumed (and dropped) there rather than left without an owner.
		assertThrows(java.lang.Exception.class, () -> EntryMut.newEntry(null, value));
		assertEquals(0L, value.getPtr());
		assertDoesNotThrow(() -> EntryMut.newEntry("k", ValueMut.createString("y")));
	}

	@Test
	void failedCollectionCreationStillConsumesEveryValue() {
		// The first element was already handed over, so the native call fails on
		// it. The live elements after it must still be consumed, since Java marks
		// every element moved, rather than left without an owner.
		final ValueMut consumed = ValueMut.createString("a");
		EntryMut.newEntry("k", consumed);
		final ValueMut live = ValueMut.createString("b");
		assertThrows(NullPointerException.class, () -> ValueMut.createArray(Arrays.asList(consumed, live)));
		assertEquals(0L, live.getPtr());

		final EntryMut consumedEntry = EntryMut.newEntry("k", ValueMut.createString("a"));
		ValueMut.createObject(Arrays.asList(consumedEntry));
		final EntryMut liveEntry = EntryMut.newEntry("l", ValueMut.createString("b"));
		assertThrows(NullPointerException.class, () -> ValueMut.createObject(Arrays.asList(consumedEntry, liveEntry)));
		assertEquals(0L, liveEntry.getPtr());
	}

	@Test
	void passingTheSameValueTwiceIsRejectedNotFreedTwice() {
		// The native side takes ownership of each element. Taking the same pointer
		// twice would free it twice in release builds (debug builds only caught it
		// through allocation tracking), so a repeated element is refused.
		final ValueMut value = ValueMut.createString("a");
		final SurrealException e = assertThrows(SurrealException.class,
				() -> ValueMut.createArray(Arrays.asList(value, value)));
		assertEquals("The same MutableValue was passed more than once", e.getMessage());
		assertEquals(0L, value.getPtr());

		final EntryMut entry = EntryMut.newEntry("k", ValueMut.createString("a"));
		final SurrealException e2 = assertThrows(SurrealException.class,
				() -> ValueMut.createObject(Arrays.asList(entry, entry)));
		assertEquals("The same MutableObjectEntry was passed more than once", e2.getMessage());
		assertEquals(0L, entry.getPtr());
	}

	@Test
	void unclosedObjectsAreCleanedUp() throws InterruptedException {
		// Values, iterators and entries used to have no native destructor at all;
		// this exercises the cleaner path for each of them.
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("CREATE person:1 SET name = 'a', tags = ['x']");
			for (int i = 0; i < 200; i++) {
				final Value v = surreal.query("SELECT * FROM person").take(0);
				v.getArray().iterator().forEachRemaining(e -> e.getObject().iterator().forEachRemaining(Entry::getKey));
				surreal.select("person").forEachRemaining(Value::getObject);
				Id.from(1, "a");
			}
		}
		System.gc();
		Thread.sleep(100);
	}
}
