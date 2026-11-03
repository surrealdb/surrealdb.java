package com.surrealdb;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import static com.surrealdb.Helpers.jaime;
import static com.surrealdb.Helpers.tobie;
import com.surrealdb.pojos.Person;

/**
 * SurrealDB calls must not run on the calling Java thread, which can have as
 * little as 1 MiB of stack (the Linux default). They run on Tokio workers with
 * large stacks, so a caller with a tiny stack must still work.
 */
public class SmallStackTests {

	private static final long STACK_BYTES = 256 * 1024;
	private static final long JOIN_TIMEOUT_MS = 60_000;

	@Test
	void embeddedOperationsOnSmallStackThread() throws InterruptedException {
		final AtomicReference<Throwable> failure = new AtomicReference<>();
		final Thread thread = new Thread(null, () -> {
			try {
				runOperations();
			} catch (Throwable t) {
				failure.set(t);
			}
		}, "small-stack", STACK_BYTES);
		thread.start();
		thread.join(JOIN_TIMEOUT_MS);
		assertFalse(thread.isAlive(), "The small-stack thread did not finish in time");
		final Throwable t = failure.get();
		if (t != null) {
			fail("Operation failed on a " + STACK_BYTES + " byte stack thread", t);
		}
	}

	private static void runOperations() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// create
			final RecordId id = new RecordId("person", 1);
			surreal.create(id, tobie);
			// select
			final Optional<Person> selected = surreal.select(Person.class, id);
			assertTrue(selected.isPresent());
			assertEquals("Tobie", selected.get().name);
			// query
			final Response response = surreal.query("SELECT * FROM person");
			assertEquals(1, response.take(0).getArray().len());
			// update
			final Person updated = surreal.update(Person.class, id, UpType.MERGE, jaime);
			assertEquals("Jaime", updated.name);
			// transaction: begin, query, commit
			final Transaction txn = surreal.beginTransaction();
			txn.query("CREATE person:2 SET name = 'Tx'");
			txn.commit();
			assertEquals(2, surreal.query("SELECT * FROM person").take(0).getArray().len());
			// delete
			surreal.delete(id);
			assertEquals(Optional.empty(), surreal.select(id));
		}
	}
}
