// DO NOT import com.surrealdb.Surreal or com.surrealdb.LiveStream from this
// file. These tests assert that publicly-constructible Native-backed POJOs
// (RecordId, Id, Array) auto-load the native library by themselves, without
// any prior reference to Surreal/LiveStream. Touching those classes here
// would silently mask the bug under test by loading the library through
// their (no-longer-present) static initializers.
package com.surrealdb;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the contract that instantiating a Surreal POJO does not
 * require a running {@code Surreal} instance. The native library must load
 * transparently the first time any {@code Native} subclass is touched.
 */
public class PojoStandaloneTests {

	@Test
	void recordIdWithStringKey() {
		RecordId rec = new RecordId("myRec", "foo");
		assertNotNull(rec.toString());
		assertEquals("myRec", rec.getTable());
		assertEquals("foo", rec.getId().getString());
		rec.hashCode();
	}

	@Test
	void recordIdWithLongKey() {
		RecordId rec = new RecordId("myRec", 42L);
		assertNotNull(rec.toString());
		assertEquals("myRec", rec.getTable());
		assertEquals(42L, rec.getId().getLong());
		rec.hashCode();
	}

	@Test
	void recordIdWithUuidKey() {
		UUID uuid = UUID.randomUUID();
		RecordId rec = new RecordId("myRec", uuid);
		assertNotNull(rec.toString());
		assertEquals("myRec", rec.getTable());
		assertEquals(uuid, rec.getId().getUuid());
		rec.hashCode();
	}

	@Test
	void idFromLong() {
		Id id = Id.from(7L);
		assertTrue(id.isLong());
		assertEquals(7L, id.getLong());
		assertNotNull(id.toString());
		id.hashCode();
	}

	@Test
	void idFromString() {
		Id id = Id.from("abc");
		assertTrue(id.isString());
		assertEquals("abc", id.getString());
		assertNotNull(id.toString());
		id.hashCode();
	}

	@Test
	void idFromUuid() {
		UUID uuid = UUID.randomUUID();
		Id id = Id.from(uuid);
		assertTrue(id.isUuid());
		assertEquals(uuid, id.getUuid());
		assertNotNull(id.toString());
		id.hashCode();
	}

	@Test
	void idFromCompositeArray() {
		Id id = Id.from("a", 1L, true);
		assertTrue(id.isArray());
		assertNotNull(id.toString());
		id.hashCode();
	}

	@Test
	void arrayOfHeterogeneousElements() {
		Array array = Array.of("a", 1L, true);
		assertEquals(3, array.len());
		assertNotNull(array.toString());
		array.hashCode();
	}
}
