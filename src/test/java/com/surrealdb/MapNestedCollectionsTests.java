package com.surrealdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Inventory;
import com.surrealdb.pojos.Leaderboard;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Team;
import com.surrealdb.pojos.Translations;
import com.surrealdb.pojos.Warehouse;

public class MapNestedCollectionsTests {

	@Test
	void roundTripMapOfStringLists() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Map<String, java.util.List<String>> categories = new HashMap<>();
			categories.put("fruits", Arrays.asList("apple", "banana"));
			categories.put("vegetables", Arrays.asList("carrot"));
			final Inventory inventory = new Inventory(categories);
			// Write path
			final Inventory created = surreal.create(Inventory.class, "inventory", inventory).get(0);
			assertNotNull(created.id);
			// Read path
			final Optional<Inventory> selected = surreal.select(Inventory.class, created.id);
			assertTrue(selected.isPresent());
			selected.get().id = null;
			assertEquals(inventory, selected.get());
		}
	}

	@Test
	void roundTripMapOfPojoLists() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Map<String, java.util.List<Name>> members = new HashMap<>();
			members.put("engineering",
					Arrays.asList(new Name("Tobie", "Morgan Hitchcock"), new Name("Jaime", "Morgan Hitchcock")));
			members.put("design", Arrays.asList(new Name("Ada", "Lovelace")));
			final Team team = new Team(members);
			// Write path
			final Team created = surreal.create(Team.class, "team", team).get(0);
			assertNotNull(created.id);
			// Read path
			final Optional<Team> selected = surreal.select(Team.class, created.id);
			assertTrue(selected.isPresent());
			selected.get().id = null;
			assertEquals(team, selected.get());
		}
	}

	@Test
	void roundTripNestedMaps() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Map<String, String> en = new HashMap<>();
			en.put("hello", "hello");
			en.put("bye", "goodbye");
			final Map<String, String> fr = new HashMap<>();
			fr.put("hello", "bonjour");
			fr.put("bye", "au revoir");
			final Map<String, Map<String, String>> entries = new HashMap<>();
			entries.put("en", en);
			entries.put("fr", fr);
			final Translations translations = new Translations(entries);
			// Write path
			final Translations created = surreal.create(Translations.class, "translations", translations).get(0);
			assertNotNull(created.id);
			// Read path
			final Optional<Translations> selected = surreal.select(Translations.class, created.id);
			assertTrue(selected.isPresent());
			selected.get().id = null;
			assertEquals(translations, selected.get());
		}
	}

	@Test
	void roundTripMapOfListsOfMaps() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Map<String, String> shelfA = new HashMap<>();
			shelfA.put("sku", "A-1");
			shelfA.put("label", "bolts");
			final Map<String, String> shelfB = new HashMap<>();
			shelfB.put("sku", "B-2");
			shelfB.put("label", "nuts");
			final Map<String, java.util.List<Map<String, String>>> sections = new HashMap<>();
			sections.put("hardware", Arrays.asList(shelfA, shelfB));
			final Map<String, java.util.List<Integer>> stock = new HashMap<>();
			stock.put("hardware", Arrays.asList(7, 12));
			final Warehouse warehouse = new Warehouse(sections, new HashMap<>(), stock);
			// Write path
			final Warehouse created = surreal.create(Warehouse.class, "warehouse", warehouse).get(0);
			assertNotNull(created.id);
			// Read path
			final Optional<Warehouse> selected = surreal.select(Warehouse.class, created.id);
			assertTrue(selected.isPresent());
			// Scalar elements hydrate with the declared element type.
			assertEquals(Integer.class, selected.get().stock.get("hardware").get(0).getClass());
			selected.get().id = null;
			assertEquals(warehouse, selected.get());
		}
	}

	@Test
	void roundTripMapOfListsOfPojoLists() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Map<String, java.util.List<java.util.List<Name>>> shifts = new HashMap<>();
			shifts.put("monday",
					Arrays.asList(
							Arrays.asList(new Name("Tobie", "Morgan Hitchcock"), new Name("Jaime", "Morgan Hitchcock")),
							Arrays.asList(new Name("Ada", "Lovelace"))));
			final Warehouse warehouse = new Warehouse(new HashMap<>(), shifts, new HashMap<>());
			// Write path
			final Warehouse created = surreal.create(Warehouse.class, "warehouse", warehouse).get(0);
			assertNotNull(created.id);
			// Read path
			final Optional<Warehouse> selected = surreal.select(Warehouse.class, created.id);
			assertTrue(selected.isPresent());
			selected.get().id = null;
			assertEquals(warehouse, selected.get());
		}
	}

	@Test
	void roundTripConcreteAndSortedMapFieldTypes() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final java.util.TreeMap<String, Long> scores = new java.util.TreeMap<>();
			scores.put("alice", 3L);
			scores.put("bob", 5L);
			final java.util.LinkedHashMap<String, String> labels = new java.util.LinkedHashMap<>();
			labels.put("alice", "gold");
			labels.put("bob", "silver");
			final java.util.TreeMap<String, Long> ranks = new java.util.TreeMap<>();
			ranks.put("alice", 2L);
			ranks.put("bob", 1L);
			final Leaderboard leaderboard = new Leaderboard(scores, labels, ranks);
			// Write path
			final Leaderboard created = surreal.create(Leaderboard.class, "leaderboard", leaderboard).get(0);
			assertNotNull(created.id);
			// Read path: the declared map types must be honoured on set.
			final Optional<Leaderboard> selected = surreal.select(Leaderboard.class, created.id);
			assertTrue(selected.isPresent());
			assertEquals(java.util.TreeMap.class, selected.get().scores.getClass());
			assertEquals(java.util.LinkedHashMap.class, selected.get().labels.getClass());
			assertTrue(selected.get().ranks instanceof java.util.SortedMap);
			selected.get().id = null;
			assertEquals(leaderboard, selected.get());
		}
	}

	@Test
	void writePathStoresNestedCollections() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Map<String, java.util.List<String>> categories = new HashMap<>();
			categories.put("fruits", Arrays.asList("apple", "banana"));
			final Inventory inventory = new Inventory(categories);
			// Write via the untyped API so only ValueBuilder is exercised
			final Value created = surreal.create("inventory", inventory).get(0);
			assertTrue(created.isObject());
			final Value stored = created.getObject().get("categories");
			assertTrue(stored.isObject());
			final Value fruits = stored.getObject().get("fruits");
			assertTrue(fruits.isArray());
			assertEquals(2, fruits.getArray().len());
			assertEquals("apple", fruits.getArray().get(0).getString());
			assertEquals("banana", fruits.getArray().get(1).getString());
		}
	}
}
