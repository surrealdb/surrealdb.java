package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 * Query results can be released deterministically with close() or
 * try-with-resources, instead of waiting for the garbage collector to release
 * their native memory (issue #199).
 */
public class ResultCloseTests {

	@Test
	void resultsWorkWithTryWithResources() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("CREATE person:1 SET name = 'tobie', tags = ['a', 'b']");
			final Value leaked;
			try (final Response response = surreal.query("SELECT * FROM person");
					final Value value = response.take(0);
					final Array rows = value.getArray();
					final Value row = rows.get(0);
					final Object person = row.getObject()) {
				assertEquals("tobie", person.get("name").getString());
				leaked = value;
			}
			// Using a closed instance fails cleanly instead of touching freed memory.
			assertThrows(NullPointerException.class, leaked::getArray);
		}
	}

	@Test
	void closeIsIdempotent() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Response response = surreal.query("RETURN 1");
			final Value value = response.take(0);
			response.close();
			assertDoesNotThrow(response::close);
			value.close();
			assertDoesNotThrow(value::close);
			assertThrows(NullPointerException.class, value::getLong);
		}
	}

	@Test
	void closingAParentLeavesWhatWasTakenFromItUsable() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Response response = surreal.query("RETURN { a: [1, 2, 3], b: 'x' }");
			final Value value = response.take(0);
			response.close();
			final Object object = value.getObject();
			final Array array = object.get("a").getArray();
			final Entry entry = object.iterator().next();
			value.close();
			object.close();
			// The array, the entry and its value each hold their own reference.
			assertEquals(3, array.len());
			assertEquals(2L, array.get(1).getLong());
			assertEquals("a", entry.getKey());
			final Value entryValue = entry.getValue();
			entry.close();
			assertEquals(3, entryValue.getArray().len());
			array.close();
			entryValue.close();
		}
	}
}
