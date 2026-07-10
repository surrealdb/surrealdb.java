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
}
