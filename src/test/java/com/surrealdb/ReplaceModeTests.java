package com.surrealdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RootCredential;

/**
 * {@link UpType#REPLACE}: update and upsert with {@code REPLACE}, which swaps
 * the whole record for the given data.
 */
public class ReplaceModeTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	private static void withEmbedded(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			test.accept(surreal);
		}
	}

	private static void withServer(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			try {
				surreal.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
			}
			surreal.signin(new RootCredential("root", "root"));
			surreal.useNs("test_ns").useDb("replace_" + System.nanoTime());
			test.accept(surreal);
		}
	}

	private static Map<String, java.lang.Object> named(String name) {
		final Map<String, java.lang.Object> m = new HashMap<>();
		m.put("name", name);
		return m;
	}

	/** Every record was rewritten to just {@code name}: no field survived. */
	private static void assertReplaced(List<Value> records, int count, String name) {
		assertEquals(count, records.size());
		for (final Value v : records) {
			assertEquals(name, v.getObject().get("name").getString());
			assertTrue(v.getObject().get("age").isNone());
		}
	}

	private static void replace(Surreal surreal) {
		surreal.query("CREATE person:1 SET name = 'a', age = 3; CREATE person:2 SET name = 'b', age = 4");

		// MERGE keeps the fields it is not given; REPLACE drops them.
		final Value merged = surreal.update(new RecordId("person", 1L), UpType.MERGE, named("m"));
		assertEquals(3L, merged.getObject().get("age").getLong());
		final Value replaced = surreal.update(new RecordId("person", 1L), UpType.REPLACE, named("r"));
		assertEquals("r", replaced.getObject().get("name").getString());
		assertTrue(replaced.getObject().get("age").isNone());

		// upsert REPLACE creates a missing record.
		final Value upserted = surreal.upsert(new RecordId("person", 3L), UpType.REPLACE, named("u"));
		assertEquals("u", upserted.getObject().get("name").getString());

		// Table-wide REPLACE rewrites every record.
		final List<Value> all = new ArrayList<>();
		surreal.update("person", UpType.REPLACE, named("t")).forEachRemaining(all::add);
		assertEquals(3, all.size());
		for (final Value v : all) {
			assertEquals("t", v.getObject().get("name").getString());
			assertFalse(v.getObject().get("age").isLong());
		}

		// A range REPLACE rewrites every record in the range (both ends included).
		surreal.query("CREATE pet:1 SET name = 'p', age = 1; CREATE pet:2 SET name = 'q', age = 2;"
				+ " CREATE pet:3 SET name = 's', age = 3");
		// The range methods return the first record written; select reads them all.
		final RecordIdRange first2 = new RecordIdRange("pet", Id.from(1L), Id.from(2L));
		assertReplaced(Collections.singletonList(surreal.update(first2, UpType.REPLACE, named("rr"))), 1, "rr");
		assertReplaced(surreal.select(first2), 2, "rr");
		final RecordIdRange last2 = new RecordIdRange("pet", Id.from(2L), Id.from(3L));
		assertReplaced(Collections.singletonList(surreal.upsert(last2, UpType.REPLACE, named("ur"))), 1, "ur");
		assertReplaced(surreal.select(last2), 2, "ur");
	}

	@Test
	void replaceEmbedded() {
		withEmbedded(ReplaceModeTests::replace);
	}

	@Test
	void replaceRemote() {
		withServer(ReplaceModeTests::replace);
	}
}
