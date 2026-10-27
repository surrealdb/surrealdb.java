package com.surrealdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
		// The native side takes ownership of each element. Handing it the same
		// pointer twice would free it twice, so a repeated element is refused before
		// anything is handed over, and the value stays usable.
		final ValueMut value = ValueMut.createString("a");
		final IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> ValueMut.createArray(Arrays.asList(value, value)));
		assertEquals("The same ValueMut was passed more than once", e.getMessage());
		assertNotEquals(0L, value.getPtr());
		assertDoesNotThrow(() -> ValueMut.createArray(Arrays.asList(value)));

		final EntryMut entry = EntryMut.newEntry("k", ValueMut.createString("a"));
		final IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class,
				() -> ValueMut.createObject(Arrays.asList(entry, entry)));
		assertEquals("The same EntryMut was passed more than once", e2.getMessage());
		assertNotEquals(0L, entry.getPtr());
	}

	@Test
	void nullElementIsRejectedBeforeAnythingIsTaken() {
		// A null must not leak the elements before it: they stay with their owner.
		final ValueMut v1 = ValueMut.createString("a");
		final NullPointerException e = assertThrows(NullPointerException.class,
				() -> ValueMut.createArray(Arrays.asList(v1, null)));
		assertEquals("A Native element cannot be null", e.getMessage());
		assertNotEquals(0L, v1.getPtr());
		assertDoesNotThrow(() -> ValueMut.createArray(Arrays.asList(v1)));

		// Two nulls used to fail with an unrelated NullPointerException.
		final ValueMut v2 = ValueMut.createString("b");
		final NullPointerException e2 = assertThrows(NullPointerException.class,
				() -> ValueMut.createArray(Arrays.asList(v2, null, null)));
		assertEquals("A Native element cannot be null", e2.getMessage());
		assertNotEquals(0L, v2.getPtr());
		final NullPointerException e3 = assertThrows(NullPointerException.class,
				() -> ValueMut.createArray(Arrays.asList(null, null)));
		assertEquals("A Native element cannot be null", e3.getMessage());
		assertDoesNotThrow(() -> ValueMut.createArray(Arrays.asList(v2)));

		final EntryMut entry = EntryMut.newEntry("k", ValueMut.createString("a"));
		assertThrows(NullPointerException.class, () -> ValueMut.createObject(Arrays.asList(entry, null)));
		assertNotEquals(0L, entry.getPtr());
		assertDoesNotThrow(() -> ValueMut.createObject(Arrays.asList(entry)));
	}

	private static ValueMut newContent() {
		return ValueMut.createObject(Arrays.asList(EntryMut.newEntry("name", ValueMut.createString("x"))));
	}

	@Test
	void callerSuppliedValueMutSurvivesWriteCalls() {
		// The SDK frees the temporary ValueMuts it converts, but a ValueMut passed in
		// by the caller belongs to the caller and must stay usable.
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");

			final ValueMut forCreate = newContent();
			surreal.create("person", forCreate);
			assertNotEquals(0L, forCreate.getPtr());
			assertDoesNotThrow(() -> surreal.create("person", forCreate));

			final ValueMut forRecordCreate = newContent();
			surreal.create(new RecordId("person", 1), forRecordCreate);
			assertNotEquals(0L, forRecordCreate.getPtr());
			assertDoesNotThrow(() -> surreal.create(new RecordId("person", 2), forRecordCreate));

			final ValueMut forInsert = newContent();
			surreal.insert("person", forInsert);
			assertNotEquals(0L, forInsert.getPtr());
			assertDoesNotThrow(() -> surreal.insert("person", forInsert));

			final ValueMut forUpdate = newContent();
			surreal.update("person", UpType.MERGE, forUpdate).forEachRemaining(v -> {
			});
			assertNotEquals(0L, forUpdate.getPtr());
			assertDoesNotThrow(() -> surreal.update(new RecordId("person", 1), UpType.MERGE, forUpdate));

			final ValueMut param = ValueMut.createString("x");
			final Map<String, java.lang.Object> params = new HashMap<>();
			params.put("name", param);
			params.put("other", "temporary");
			assertEquals(1,
					surreal.query("SELECT * FROM person WHERE name = $name LIMIT 1", params).take(0).getArray().len());
			assertNotEquals(0L, param.getPtr());
			assertDoesNotThrow(() -> surreal.query("RETURN $name", params));

			final ValueMut arg = ValueMut.createString("x");
			surreal.run("string::uppercase", arg);
			assertNotEquals(0L, arg.getPtr());
			assertDoesNotThrow(() -> surreal.run("string::uppercase", arg));

			// Also when the caller's value is wrapped in an Optional.
			final ValueMut optional = newContent();
			surreal.create("person", java.util.Optional.of(optional));
			assertNotEquals(0L, optional.getPtr());
			assertDoesNotThrow(() -> surreal.create("person", optional));
		}
	}

	@Test
	void aValueCanOnlyBeHandedOverOnce() throws InterruptedException {
		// takePtr() clears the pointer before the native call, so when two threads
		// hand over the same transaction only one of them passes the pointer on.
		for (int round = 0; round < 50; round++) {
			try (final Surreal surreal = new Surreal()) {
				surreal.connect("memory").useNs("test_ns").useDb("test_db");
				final Transaction txn = surreal.beginTransaction();
				final CountDownLatch start = new CountDownLatch(1);
				final AtomicInteger succeeded = new AtomicInteger();
				final List<Thread> threads = new ArrayList<>();
				for (int t = 0; t < 2; t++) {
					final boolean commit = t == 0;
					final Thread thread = new Thread(() -> {
						try {
							start.await();
							if (commit) {
								txn.commit();
							} else {
								txn.cancel();
							}
							succeeded.incrementAndGet();
						} catch (Throwable e) {
							// The loser sees a released transaction.
						}
					});
					thread.start();
					threads.add(thread);
				}
				start.countDown();
				for (final Thread thread : threads) {
					thread.join();
				}
				assertEquals(1, succeeded.get());
				assertEquals(0L, txn.getPtr());
			}
		}
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

	/** A POJO with a ValueMut field. */
	public static class WithValueMut {
		public String name;
		public ValueMut payload;

		public WithValueMut(String name, ValueMut payload) {
			this.name = name;
			this.payload = payload;
		}
	}

	@Test
	void callerValueMutNestedInMapSurvivesAndIsReusable() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final ValueMut vm = ValueMut.createString("hello");
			final Map<String, java.lang.Object> map = Collections.singletonMap("avatar", vm);
			// The same map, and the same ValueMut, work in several calls.
			for (int i = 0; i < 3; i++) {
				assertEquals(1, surreal.create("person", map).size());
				final Value v = surreal.query("RETURN $m", Collections.singletonMap("m", map)).take(0);
				assertEquals("hello", v.getObject().get("avatar").getString());
				assertNotEquals(0L, vm.getPtr());
			}
			assertEquals("'hello'", vm.toString());
		}
	}

	@Test
	void callerValueMutNestedInListAndOptionalSurvives() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final ValueMut vm = ValueMut.createLong(7);
			final List<java.lang.Object> list = Arrays.asList(vm, java.util.Optional.of(vm), 1L);
			for (int i = 0; i < 3; i++) {
				final Value v = surreal.query("RETURN $l", Collections.singletonMap("l", list)).take(0);
				assertEquals(3, v.getArray().len());
				assertEquals(7L, v.getArray().get(0).getLong());
				assertEquals(7L, v.getArray().get(1).getLong());
				assertNotEquals(0L, vm.getPtr());
			}
			// At the top level it is only borrowed, too.
			assertEquals(7L, surreal.query("RETURN $v", Collections.singletonMap("v", vm)).take(0).getLong());
			assertEquals(7L, surreal.query("RETURN $v", Collections.singletonMap("v", java.util.Optional.of(vm)))
					.take(0).getLong());
			assertNotEquals(0L, vm.getPtr());
		}
	}

	@Test
	void callerValueMutInPojoFieldSurvives() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final ValueMut vm = ValueMut.createString("data");
			final WithValueMut pojo = new WithValueMut("n", vm);
			for (int i = 0; i < 3; i++) {
				final List<Value> created = surreal.create("pojo", pojo);
				assertEquals(1, created.size());
				assertEquals("data", created.get(0).getObject().get("payload").getString());
				assertNotEquals(0L, vm.getPtr());
			}
			// Several create contents sharing the one ValueMut.
			assertEquals(2, surreal.create("pojo", pojo, new WithValueMut("m", vm)).size());
			assertNotEquals(0L, vm.getPtr());
		}
	}

	@Test
	void sameCallerValueMutUnderTwoKeys() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final ValueMut vm = ValueMut.createString("shared");
			final Map<String, java.lang.Object> map = new LinkedHashMap<>();
			map.put("a", vm);
			map.put("b", vm);
			map.put("c", Arrays.asList(vm, vm));
			final Value v = surreal.query("RETURN $m", Collections.singletonMap("m", map)).take(0);
			assertEquals("shared", v.getObject().get("a").getString());
			assertEquals("shared", v.getObject().get("b").getString());
			assertEquals("shared", v.getObject().get("c").getArray().get(1).getString());
			assertNotEquals(0L, vm.getPtr());
			// And the same ValueMut in two fields of a POJO-free object call.
			final Value created = surreal.create("two", map).get(0);
			assertEquals("shared", created.getObject().get("a").getString());
			assertEquals("shared", created.getObject().get("b").getString());
		}
	}

	@Test
	void geometryCollectionSnapshotsAnyList() {
		final Geometry p = Geometry.point(1.0, 2.0);
		final Geometry q = Geometry.point(3.0, 4.0);
		assertEquals(2, Geometry.geometryCollection(Arrays.asList(p, q)).getGeometryCollection().size());
		assertEquals(2,
				Geometry.geometryCollection(new LinkedList<>(Arrays.asList(p, q))).getGeometryCollection().size());
		assertEquals(0, Geometry.geometryCollection(new ArrayList<>()).getGeometryCollection().size());
		// The inputs are only borrowed.
		assertNotEquals(0L, p.getPtr());
		assertNotEquals(0L, q.getPtr());
		assertThrows(NullPointerException.class, () -> Geometry.geometryCollection(Arrays.asList(p, null)));
		assertNotEquals(0L, p.getPtr());
	}

	@Test
	void failedConversionPartwayThrowsAndLeavesLaterCallsWorking() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final ValueMut callerOwned = ValueMut.createString("keep");
			final java.lang.Object unsupported = new java.lang.Object();
			// Entries converted before the failing one are freed (a caller-owned ValueMut
			// is spared); the failure itself reaches the caller.
			final Map<String, java.lang.Object> params = new LinkedHashMap<>();
			params.put("a", "x");
			params.put("b", Arrays.asList(1L, 2L));
			params.put("c", callerOwned);
			params.put("d", unsupported);
			assertThrows(RuntimeException.class, () -> surreal.query("RETURN $a", params));
			// Same through varargs contents.
			assertThrows(RuntimeException.class, () -> surreal.create("person", Collections.singletonMap("k", "v"),
					callerOwned, java.math.BigInteger.ONE));
			// And inside a nested Collection, Map, and Optional.
			final Map<String, java.lang.Object> nested = new LinkedHashMap<>();
			nested.put("ok", callerOwned);
			nested.put("list", Arrays.asList("a", callerOwned, java.util.Optional.of(java.math.BigInteger.ONE)));
			assertThrows(RuntimeException.class,
					() -> surreal.query("RETURN $n", Collections.singletonMap("n", nested)));
			final Map<String, java.lang.Object> badMap = new LinkedHashMap<>();
			badMap.put("ok", "v");
			badMap.put("bad", unsupported);
			assertThrows(RuntimeException.class,
					() -> surreal.query("RETURN $n", Collections.singletonMap("n", badMap)));
			assertThrows(RuntimeException.class,
					() -> surreal.create("person", new WithValueMut("n", callerOwned), unsupported));
			// Nothing is left broken.
			assertNotEquals(0L, callerOwned.getPtr());
			assertEquals("keep",
					surreal.query("RETURN $v", Collections.singletonMap("v", callerOwned)).take(0).getString());
			assertEquals(1, surreal.create("person", Collections.singletonMap("k", "v")).size());
			assertNotNull(surreal.query("RETURN $a", Collections.singletonMap("a", "x")).take(0));
			assertTrue(callerOwned.getPtr() != 0);
		}
	}
}
