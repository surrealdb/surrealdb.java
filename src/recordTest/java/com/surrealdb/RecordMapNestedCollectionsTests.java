package com.surrealdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.CatalogRecord;
import com.surrealdb.pojos.DepotRecord;
import com.surrealdb.pojos.NameRecord;

public class RecordMapNestedCollectionsTests {

	@Test
	void roundTripNestedCollectionsInMaps() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");

			final Map<String, List<String>> categories = new HashMap<>();
			categories.put("fruits", Arrays.asList("apple", "banana"));
			categories.put("vegetables", Arrays.asList("carrot"));

			final Map<String, List<NameRecord>> teams = new HashMap<>();
			teams.put("engineering", Arrays.asList(new NameRecord("Tobie", "Morgan Hitchcock")));
			teams.put("design", Arrays.asList(new NameRecord("Ada", "Lovelace"), new NameRecord("Grace", "Hopper")));

			final Map<String, String> fr = new HashMap<>();
			fr.put("hello", "bonjour");
			final Map<String, Map<String, String>> translations = new HashMap<>();
			translations.put("fr", fr);

			final CatalogRecord catalog = new CatalogRecord(null, categories, teams, translations);
			final CatalogRecord created = surreal.create(CatalogRecord.class, "catalog", catalog).get(0);
			assertNotNull(created.id());

			final Optional<CatalogRecord> selected = surreal.select(CatalogRecord.class, created.id());
			assertTrue(selected.isPresent());
			assertEquals(categories, selected.get().categories());
			assertEquals(teams, selected.get().teams());
			assertEquals(translations, selected.get().translations());
		}
	}

	@Test
	void roundTripDeeperNestingAndConcreteMapTypes() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");

			final Map<String, String> shelf = new HashMap<>();
			shelf.put("sku", "A-1");
			final Map<String, List<Map<String, String>>> sections = new HashMap<>();
			sections.put("hardware", Arrays.asList(shelf));

			final Map<String, List<List<NameRecord>>> shifts = new HashMap<>();
			shifts.put("monday", Arrays.asList(Arrays.asList(new NameRecord("Ada", "Lovelace")),
					Arrays.asList(new NameRecord("Grace", "Hopper"))));

			final java.util.TreeMap<String, Long> scores = new java.util.TreeMap<>();
			scores.put("alice", 3L);
			scores.put("bob", 5L);

			final DepotRecord depot = new DepotRecord(null, sections, shifts, scores);
			final DepotRecord created = surreal.create(DepotRecord.class, "depot", depot).get(0);
			assertNotNull(created.id());

			final Optional<DepotRecord> selected = surreal.select(DepotRecord.class, created.id());
			assertTrue(selected.isPresent());
			assertEquals(sections, selected.get().sections());
			assertEquals(shifts, selected.get().shifts());
			assertEquals(scores, selected.get().scores());
			assertEquals(java.util.TreeMap.class, selected.get().scores().getClass());
		}
	}
}
