package com.surrealdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for session variables: {@link Surreal#set(String, java.lang.Object)}
 * defines a variable usable as {@code $key} in subsequent queries on the same
 * session, and {@link Surreal#unset(String)} removes it.
 */
public class SessionVariableTests {

	@Test
	void letDoesNotPersistAcrossQueryCalls() {
		// Reproduction of the gap that set() fills: a LET statement only lives for
		// the remaining statements of the same query() call.
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			{ // Within a single query() call, LET carries into later statements
				final Response response = surreal.query("LET $x = 42; RETURN $x");
				assertEquals(42, response.take(1).getLong());
			}
			{ // Across separate query() calls, the LET variable is gone
				surreal.query("LET $y = 42");
				final Value y = surreal.query("RETURN $y").take(0);
				assertTrue(y.isNone());
			}
		}
	}

	@Test
	void setThenReturnAcrossMultipleQueryCalls() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			assertSame(surreal, surreal.set("x", 42));
			// The variable persists across separate query() calls
			assertEquals(42, surreal.query("RETURN $x").take(0).getLong());
			assertEquals(42, surreal.query("RETURN $x").take(0).getLong());
			// And is usable inside statements
			final Response response = surreal.query("RETURN $x + 1");
			assertEquals(43, response.take(0).getLong());
		}
	}

	@Test
	void setOverwritesPreviousValue() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.set("x", 1);
			surreal.set("x", "two");
			final Value x = surreal.query("RETURN $x").take(0);
			assertTrue(x.isString());
			assertEquals("two", x.getString());
		}
	}

	@Test
	void setMapValue() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Map<String, java.lang.Object> name = new HashMap<>();
			name.put("first", "Tobie");
			name.put("last", "Morgan Hitchcock");
			surreal.set("name", name);
			final Value value = surreal.query("RETURN $name").take(0);
			assertTrue(value.isObject());
			final Object object = value.getObject();
			assertEquals("Tobie", object.get("first").getString());
			assertEquals("Morgan Hitchcock", object.get("last").getString());
			// Field access on the variable works too
			assertEquals("Tobie", surreal.query("RETURN $name.first").take(0).getString());
		}
	}

	@Test
	void setListValue() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final List<java.lang.Object> tags = Arrays.asList("CEO", "CTO", 3L);
			surreal.set("tags", tags);
			final Value value = surreal.query("RETURN $tags").take(0);
			assertTrue(value.isArray());
			final Array array = value.getArray();
			assertEquals(3, array.len());
			assertEquals("CEO", array.get(0).getString());
			assertEquals("CTO", array.get(1).getString());
			assertEquals(3, array.get(2).getLong());
			assertEquals(3, surreal.query("RETURN array::len($tags)").take(0).getLong());
		}
	}

	@Test
	void setRecordIdValue() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("CREATE person:1 SET name = 'Tobie'");
			surreal.set("rid", new RecordId("person", 1));
			final Value value = surreal.query("RETURN $rid").take(0);
			assertTrue(value.isRecordId());
			assertEquals("person", value.getRecordId().getTable());
			assertEquals(1, value.getRecordId().getId().getLong());
			// The record id variable can be used as a query target
			final Value selected = surreal.query("SELECT * FROM $rid").take(0);
			assertTrue(selected.isArray());
			assertEquals(1, selected.getArray().len());
			assertEquals("Tobie", selected.getArray().get(0).getObject().get("name").getString());
		}
	}

	@Test
	void unsetThenReturnReturnsNone() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.set("x", 42);
			assertEquals(42, surreal.query("RETURN $x").take(0).getLong());
			assertSame(surreal, surreal.unset("x"));
			final Value x = surreal.query("RETURN $x").take(0);
			assertTrue(x.isNone());
		}
	}

	@Test
	void unsetUndefinedVariableIsNoOp() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.unset("neverDefined");
			assertTrue(surreal.query("RETURN $neverDefined").take(0).isNone());
		}
	}

	@Test
	void newSessionSnapshotsVariablesThenEvolvesIndependently() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.set("before", 1);
			try (final Surreal other = surreal.newSession()) {
				other.useNs("test_ns").useDb("test_db");
				// The new session starts with a snapshot of the parent's variables
				assertEquals(1, other.query("RETURN $before").take(0).getLong());
				// A set on the parent after the clone is not visible in the new session
				surreal.set("after", 2);
				assertTrue(other.query("RETURN $after").take(0).isNone());
				// A set on the new session is not visible in the parent
				other.set("child", 3);
				assertEquals(3, other.query("RETURN $child").take(0).getLong());
				assertTrue(surreal.query("RETURN $child").take(0).isNone());
				// An unset on the new session does not affect the parent
				other.unset("before");
				assertTrue(other.query("RETURN $before").take(0).isNone());
				assertEquals(1, surreal.query("RETURN $before").take(0).getLong());
			}
		}
	}
}
