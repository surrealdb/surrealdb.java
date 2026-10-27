package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Query results can be released deterministically with close() or
 * try-with-resources, instead of waiting for the garbage collector to release
 * their native memory (issue #199).
 */
public class ResultCloseTests {

	private static Surreal connect() {
		final Surreal surreal = new Surreal();
		surreal.connect("memory").useNs("test_ns").useDb("test_db");
		return surreal;
	}

	@Test
	void resultsWorkWithTryWithResources() {
		try (final Surreal surreal = connect()) {
			surreal.query("CREATE person:1 SET name = 'tobie', tags = ['a', 'b']").close();
			final Value closed;
			try (final Response response = surreal.query("SELECT * FROM person");
					final Value value = response.take(0);
					final Array rows = value.getArray();
					final Value row = rows.get(0);
					final Object person = row.getObject();
					final Value name = person.get("name")) {
				assertEquals("tobie", name.getString());
				closed = value;
			}
			// Using a closed instance fails cleanly instead of touching freed memory.
			assertThrows(NullPointerException.class, closed::getArray);
		}
	}

	/** Closes {@code closeable} twice, then checks that {@code use} fails. */
	private static void assertClosesOnce(AutoCloseable closeable, Executable use) throws Exception {
		closeable.close();
		assertDoesNotThrow(closeable::close);
		assertThrows(NullPointerException.class, use);
	}

	@Test
	void everyResultTypeClosesOnceAndFailsCleanlyAfterwards() throws Exception {
		try (final Surreal surreal = connect()) {
			final Response response = surreal.query("RETURN { a: [1, 2, 3] }");
			final Value value = response.take(0);
			final Object object = value.getObject();
			final Value field = object.get("a");
			final Array array = field.getArray();
			final ValueIterator values = (ValueIterator) array.iterator();
			final EntryIterator entries = (EntryIterator) object.iterator();
			final Entry entry = entries.next();

			assertClosesOnce(response, response::size);
			assertClosesOnce(value, value::getObject);
			assertClosesOnce(field, field::getArray);
			assertClosesOnce(object, object::len);
			assertClosesOnce(array, array::len);
			assertClosesOnce(values, values::hasNext);
			assertClosesOnce(entries, entries::hasNext);
			assertClosesOnce(entry, entry::getKey);
		}
	}

	@Test
	void closingAParentLeavesWhatWasTakenFromItUsable() {
		try (final Surreal surreal = connect()) {
			final Response response = surreal.query("RETURN { a: [1, 2, 3], b: 'x' }");
			final Value value = response.take(0);
			response.close();
			final Object object = value.getObject();
			final Value field = object.get("a");
			final Array array = field.getArray();
			final EntryIterator entries = (EntryIterator) object.iterator();
			final Entry entry = entries.next();
			value.close();
			field.close();
			object.close();
			entries.close();
			// The array, the entry and its value each hold their own reference.
			assertEquals(3, array.len());
			try (final Value second = array.get(1)) {
				assertEquals(2L, second.getLong());
			}
			assertEquals("a", entry.getKey());
			final Value entryValue = entry.getValue();
			entry.close();
			try (final Array entryArray = entryValue.getArray()) {
				assertEquals(3, entryArray.len());
			}
			array.close();
			entryValue.close();
		}
	}

	private static boolean exists(Surreal surreal, RecordId id) {
		return surreal.select(id).isPresent();
	}

	@Test
	void anUncommittedTransactionIsCancelledOnClose() {
		try (final Surreal surreal = connect()) {
			surreal.query("DEFINE TABLE person").close();
			try (final Transaction txn = surreal.beginTransaction()) {
				txn.query("CREATE person:cancelled").close();
			}
			assertFalse(exists(surreal, new RecordId("person", "cancelled")));
		}
	}

	@Test
	void closingACommittedTransactionDoesNothing() {
		try (final Surreal surreal = connect()) {
			try (final Transaction txn = surreal.beginTransaction()) {
				txn.query("CREATE person:committed").close();
				txn.commit();
			}
			assertTrue(exists(surreal, new RecordId("person", "committed")));
		}
	}
}
