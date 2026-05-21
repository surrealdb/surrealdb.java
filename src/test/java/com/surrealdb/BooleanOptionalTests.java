package com.surrealdb;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Booleans;

public class BooleanOptionalTests {

	@Test
	void allPresent() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Booleans b = new Booleans(true, Boolean.TRUE, Optional.of(false),
					Optional.of(Arrays.asList("a", "b")));
			final RecordId rid = new RecordId("booleans", "all");
			final Booleans created = surreal.create(Booleans.class, rid, b);
			assertEquals(b, created);
			final Booleans selected = surreal.select(Booleans.class, rid)
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			assertEquals(b, selected);
		}
	}

	@Test
	void boxedFalseRoundTrip() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Booleans b = new Booleans(false, Boolean.FALSE, Optional.of(true), Optional.empty());
			final RecordId rid = new RecordId("booleans", "boxed_false");
			final Booleans created = surreal.create(Booleans.class, rid, b);
			assertEquals(b, created);
			final Booleans selected = surreal.select(Booleans.class, rid)
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			assertEquals(b, selected);
		}
	}

	@Test
	void boxedNullIsDropped() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Booleans b = new Booleans(true, null, Optional.of(false), Optional.of(Arrays.asList("x")));
			final RecordId rid = new RecordId("booleans", "boxed_null");
			final Booleans created = surreal.create(Booleans.class, rid, b);
			assertNull(created.boxed);
			assertTrue(created.primitive);
			assertEquals(Optional.of(false), created.opt);
			final Booleans selected = surreal.select(Booleans.class, rid)
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			assertNull(selected.boxed);
			assertTrue(selected.primitive);
			assertEquals(Optional.of(false), selected.opt);
		}
	}

	@Test
	void optionalEmptyRoundTrip() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Booleans b = new Booleans(true, Boolean.TRUE, Optional.empty(), Optional.empty());
			final RecordId rid = new RecordId("booleans", "opt_empty");
			final Booleans created = surreal.create(Booleans.class, rid, b);
			assertNotNull(created.opt);
			assertEquals(Optional.empty(), created.opt);
			assertNotNull(created.optList);
			assertEquals(Optional.empty(), created.optList);
			final Booleans selected = surreal.select(Booleans.class, rid)
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			assertNotNull(selected.opt);
			assertEquals(Optional.empty(), selected.opt);
			assertNotNull(selected.optList);
			assertEquals(Optional.empty(), selected.optList);
		}
	}

	@Test
	void missingEntryDefaultsToOptionalEmpty() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("CREATE booleans:missing SET primitive = true");
			final Booleans selected = surreal.select(Booleans.class, new RecordId("booleans", "missing"))
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			assertTrue(selected.primitive);
			assertNull(selected.boxed);
			assertNotNull(selected.opt);
			assertEquals(Optional.empty(), selected.opt);
			assertNotNull(selected.optList);
			assertEquals(Optional.empty(), selected.optList);
		}
	}

	@Test
	void optionalListPresent() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final List<String> list = Arrays.asList("foo", "bar", "baz");
			final Booleans b = new Booleans(true, Boolean.TRUE, Optional.of(true), Optional.of(list));
			final RecordId rid = new RecordId("booleans", "opt_list");
			surreal.create(Booleans.class, rid, b);
			final Booleans selected = surreal.select(Booleans.class, rid)
					.orElseThrow(() -> new AssertionError("Expected record to be present"));
			assertEquals(Optional.of(list), selected.optList);
		}
	}
}
