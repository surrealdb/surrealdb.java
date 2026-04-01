package com.surrealdb;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for value types: FileRef, Table, Range, BigDecimal
 * (isBigDecimal/getBigDecimal).
 */
public class ValueTypesTests {

	@Test
	void valueBigDecimalRoundTrip() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			// Return a decimal value (SurrealDB decimal literal)
			Response r = surreal.query("RETURN 123.456789012345678901234567890123");
			Value v = r.take(0);
			assertTrue(v.isDouble() || v.isBigDecimal());
			if (v.isBigDecimal()) {
				assertEquals(0, new BigDecimal("123.456789012345678901234567890123").compareTo(v.getBigDecimal()));
			}
		}
	}

	@Test
	@Disabled("Requires experimental files feature to be enabled in SurrealDB engine")
	void valueFileFromQuery() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			Response r = surreal.query("RETURN f\"mybucket:/path/to/file.txt\"");
			Value v = r.take(0);
			assertTrue(v.isFile());
			FileRef file = v.getFile();
			assertEquals("mybucket", file.getBucket());
			assertEquals("/path/to/file.txt", file.getKey());
		}
	}

	@Test
	void valueMutCreateFile() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			ValueMut vm = ValueMut.createFile("bucket", "key");
			assertNotNull(vm);
			// Use in a query to get back as Value and verify
			Response r = surreal.queryBind("RETURN $f", java.util.Collections.singletonMap("f", vm));
			Value v = r.take(0);
			assertTrue(v.isFile());
			assertEquals("bucket", v.getFile().getBucket());
			assertEquals("/key", v.getFile().getKey()); // SDK adds leading /
		}
	}

	@Test
	void valueMutCreateTableAndQueryReturn() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			// Return a table type from SurrealQL
			Response r = surreal.query("RETURN type::table('mytable')");
			Value v = r.take(0);
			assertTrue(v.isTable());
			assertEquals("mytable", v.getTable());
		}
	}

	@Test
	void valueMutCreateTable() {
		ValueMut vm = ValueMut.createTable("sometable");
		assertNotNull(vm);
		// Conversion to Value happens via DB; we just ensure creation doesn't throw
	}

	@Test
	void isRecordIdReturnsFalseForUrlStrings() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			Response r = surreal.query("RETURN 'https://example.com/avatar/001.png'");
			Value v = r.take(0);
			assertTrue(v.isString());
			assertFalse(v.isRecordId());
		}
	}

	@Test
	void isRecordIdReturnsFalseForColonStrings() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			Response r = surreal.query("RETURN 'key:value'");
			Value v = r.take(0);
			assertTrue(v.isString());
			assertFalse(v.isRecordId());
		}
	}

	@Test
	void isRecordIdReturnsTrueForActualRecordId() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("CREATE person:1 SET name = 'Test'");
			Response r = surreal.query("SELECT * FROM person:1");
			Value v = r.take(0);
			assertTrue(v.isArray());
			Value first = v.getArray().get(0);
			assertTrue(first.isObject());
			Value id = first.getObject().get("id");
			assertTrue(id.isRecordId());
		}
	}

	@Test
	void valueRangeFromQuery() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			// Return a range 1..10
			Response r = surreal.query("RETURN 1..10");
			Value v = r.take(0);
			assertTrue(v.isRange());
			Optional<Value> start = v.getRangeStart();
			Optional<Value> end = v.getRangeEnd();
			assertTrue(start.isPresent());
			assertTrue(end.isPresent());
			assertTrue(start.get().isLong());
			assertTrue(end.get().isLong());
			assertEquals(1L, start.get().getLong());
			assertEquals(10L, end.get().getLong());
		}
	}
}
