package com.surrealdb;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.ByteData;
import com.surrealdb.pojos.Dates;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Numbers;

public class TypeTests {

	@Test
	void testNumberTypes() {
		try (final Surreal surreal = new Surreal()) {
			// Starts an embedded in memory instance
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// Create a new record
			final Numbers n = new Numbers();
			n.longPrimitive = 1;
			n.longObject = 2L;
			n.intPrimitive = 3;
			n.intObject = 4;
			n.shortPrimitive = 5;
			n.shortObject = 6;
			n.floatPrimitive = 7.5f;
			n.floatObject = 8.5f;
			n.doublePrimitive = 9.5f;
			n.doubleObject = 10.5;
			n.bigDecimal = BigDecimal.valueOf(11.5f);
			// We ingest the record
			final Numbers created = surreal.create(Numbers.class, "number", n).get(0);
			// We check that the record are matching
			assertEquals(created, n);
		}
	}

	@Test
	void testDatesTypes() {
		try (final Surreal surreal = new Surreal()) {
			// Starts an embedded in memory instance
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// Create a new record`
			final Dates d = new Dates();
			d.dateTime = ZonedDateTime.ofInstant(Instant.now().minusSeconds(120), ZoneId.of("UTC"));
			d.duration = Duration.ofMinutes(5);
			// We ingest the record
			final Dates created = surreal.create(Dates.class, "date", d).get(0);
			// We check that the records are matching
			assertEquals(created, d);
		}
	}

	@Test
	void testRecordIds() {
		try (final Surreal surreal = new Surreal()) {
			// Starts an embedded in memory instance
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// Create content
			final Name name = new Name("foo", "bar");
			// Create record ids
			final RecordId stringId = new RecordId("foo", "bar");
			final RecordId numberId = new RecordId("foo", 25565L);
			final RecordId uuidId = new RecordId("foo", UUID.randomUUID());
			// We ingest the record
			final Value created1 = surreal.create(stringId, name);
			final Value created2 = surreal.create(numberId, name);
			final Value created3 = surreal.create(uuidId, name);
			// We check that the records are matching
			assertEquals(created1.get(Name.class), name);
			assertEquals(created2.get(Name.class), name);
			assertEquals(created3.get(Name.class), name);
		}
	}

	@Test
	void testRecordIdWithArrayKey() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// Build array key from query result
			Response r = surreal.query("RETURN [1, 2, 3]");
			Value arrayValue = r.take(0);
			assertTrue(arrayValue.isArray());
			Array keyArray = arrayValue.getArray();
			RecordId rid = new RecordId("with_array_key", keyArray);
			Id id = rid.getId();
			assertTrue(id.isArray());
			Array idArray = id.getArray();
			assertEquals(3, idArray.len());
			assertEquals(1, idArray.get(0).getLong());
			final Name name = new Name("array", "key");
			final Value created = surreal.create(rid, name);
			assertEquals(created.get(Name.class), name);
			Optional<Value> selected = surreal.select(rid);
			assertTrue(selected.isPresent());
			assertEquals(name.first, selected.get().get(Name.class).first);
		}
	}

	@Test
	void testArrayOfFromVarargs() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final UUID uuid = UUID.randomUUID();
			final RecordId rid = new RecordId("doc", Array.of("myTenant", uuid));
			Id id = rid.getId();
			assertTrue(id.isArray());
			Array idArray = id.getArray();
			assertEquals(2, idArray.len());
			assertEquals("myTenant", idArray.get(0).getString());
			assertEquals(uuid, idArray.get(1).getUuid());
			final Name name = new Name("of", "varargs");
			surreal.create(rid, name);
			Optional<Value> selected = surreal.select(rid);
			assertTrue(selected.isPresent());
			assertEquals(name.first, selected.get().get(Name.class).first);
		}
	}

	@Test
	void testArrayOfEmpty() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Array empty = Array.of();
			assertEquals(0, empty.len());
		}
	}

	@Test
	void testArrayOfList() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Array a = Array.of(Arrays.asList("a", "b"));
			assertEquals(2, a.len());
			assertEquals("a", a.get(0).getString());
			assertEquals("b", a.get(1).getString());
		}
	}

	@Test
	void testArrayOfMixedPrimitives() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Array a = Array.of("a", 1L, 2.5, true);
			assertEquals(4, a.len());
			assertEquals("a", a.get(0).getString());
			assertEquals(1L, a.get(1).getLong());
			assertEquals(2.5, a.get(2).getDouble());
			assertTrue(a.get(3).getBoolean());
		}
	}

	@Test
	void testArrayOfWithNull() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Array a = Array.of("a", null, "b");
			assertEquals(3, a.len());
			assertEquals("a", a.get(0).getString());
			assertFalse(a.get(1).isString());
			assertEquals("b", a.get(2).getString());
		}
	}

	@Test
	void testArrayOfNested() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Array outer = Array.of("outer", Array.of("inner1", "inner2"));
			assertEquals(2, outer.len());
			assertEquals("outer", outer.get(0).getString());
			assertTrue(outer.get(1).isArray());
			Array inner = outer.get(1).getArray();
			assertEquals(2, inner.len());
			assertEquals("inner1", inner.get(0).getString());
			assertEquals("inner2", inner.get(1).getString());
		}
	}

	@Test
	void testArrayOfWithWrappedTypes() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final UUID uuid = UUID.randomUUID();
			final RecordId inner = new RecordId("inner", "k");
			final Array a = Array.of(inner, uuid);
			assertEquals(2, a.len());
			assertTrue(a.get(0).isRecordId());
			assertEquals(uuid, a.get(1).getUuid());
		}
	}

	@Test
	void testArrayOfRejectsUnsupportedType() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			assertThrows(IllegalArgumentException.class, () -> Array.of(new Date()));
		}
	}

	@Test
	void testIdFromArrayVarargs() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final UUID uuid = UUID.randomUUID();
			final Id id = Id.from("myTenant", uuid);
			assertTrue(id.isArray());
			Array a = id.getArray();
			assertEquals(2, a.len());
			assertEquals("myTenant", a.get(0).getString());
			assertEquals(uuid, a.get(1).getUuid());
		}
	}

	@Test
	void testRecordIdWithObjectKey() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Response r = surreal.query("RETURN { a: 1, b: 'two' }");
			Value objValue = r.take(0);
			assertTrue(objValue.isObject());
			Object keyObj = objValue.getObject();
			RecordId rid = new RecordId("with_object_key", keyObj);
			Id id = rid.getId();
			assertTrue(id.isObject());
			Object idObject = id.getObject();
			assertEquals(2, idObject.len());
			assertEquals(1, idObject.get("a").getLong());
			assertEquals("two", idObject.get("b").getString());
			final Name name = new Name("object", "key");
			final Value created = surreal.create(rid, name);
			assertEquals(created.get(Name.class), name);
			Optional<Value> selected = surreal.select(rid);
			assertTrue(selected.isPresent());
			assertEquals(name.first, selected.get().get(Name.class).first);
		}
	}

	@Test
	void testByteArray() {
		try (final Surreal surreal = new Surreal()) {
			// Starts an embedded in memory instance
			surreal.connect("memory").useNs("test_ns").useDb("test_db");

			// Test 1: Create a new record with byte[] data from raw bytes (use RecordId for
			// deterministic selection)
			final ByteData byteData = new ByteData();
			byteData.data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
			final RecordId recordIdBytes = new RecordId("bytedata", "bytedata");
			final ByteData created = surreal.create(ByteData.class, recordIdBytes, byteData);
			// We check that the records are matching
			assertEquals(created, byteData);

			// Test 2: Create a new record with byte[] data converted from string (use
			// RecordId for deterministic selection)
			final ByteData byteDataFromString = new ByteData();
			final String testString = "Hello";
			byteDataFromString.data = testString.getBytes();
			final RecordId recordIdString = new RecordId("bytedata", "string");
			final ByteData createdFromString = surreal.create(ByteData.class, recordIdString, byteDataFromString);
			// We check that the records are matching
			assertEquals(createdFromString, byteDataFromString);
			// Verify we can convert the bytes back to string
			final String retrievedString = new String(createdFromString.data);
			assertEquals(retrievedString, testString);

			// Test 3: Select from database by record id and verify byte[] is properly
			// handled
			final ByteData selectedRecord = surreal.select(ByteData.class, recordIdBytes)
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			// Compare the byte[] data content
			assertEquals(selectedRecord.data.length, byteData.data.length);
			for (int i = 0; i < selectedRecord.data.length; i++) {
				assertEquals(selectedRecord.data[i], byteData.data[i]);
			}
			// Verify the byte array content is correct
			assertEquals(selectedRecord.data.length, 5);
			assertEquals(selectedRecord.data[0], 0x01);
			assertEquals(selectedRecord.data[4], 0x05);

			// Test 4: Select the string-converted record by record id and verify conversion
			// back to
			// string
			final ByteData selectedStringRecord = surreal.select(ByteData.class, recordIdString)
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			// Compare the byte[] data content
			assertEquals(selectedStringRecord.data.length, byteDataFromString.data.length);
			for (int i = 0; i < selectedStringRecord.data.length; i++) {
				assertEquals(selectedStringRecord.data[i], byteDataFromString.data[i]);
			}
			final String selectedRetrievedString = new String(selectedStringRecord.data);
			assertEquals(selectedRetrievedString, testString);
		}
	}

}
