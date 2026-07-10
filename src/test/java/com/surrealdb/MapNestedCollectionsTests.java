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
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Team;
import com.surrealdb.pojos.Translations;

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
