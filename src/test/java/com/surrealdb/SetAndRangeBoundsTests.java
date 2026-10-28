package com.surrealdb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RootCredential;

/**
 * Regression tests for exclusive range bounds and the set type. Record ID
 * ranges used to be built with both bounds inclusive whatever the caller
 * wanted, range values lost whether each bound was inclusive, and set values
 * could be neither read nor written.
 */
public class SetAndRangeBoundsTests {

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
			surreal.useNs("test_ns").useDb("set_range_" + System.nanoTime());
			test.accept(surreal);
		}
	}

	private static List<Long> ids(List<Value> records) {
		return records.stream().map(v -> v.getObject().get("id").getRecordId().getId().getLong()).sorted()
				.collect(Collectors.toList());
	}

	private static List<Long> longs(Array array) {
		final List<Long> out = new ArrayList<>();
		array.iterator().forEachRemaining(v -> out.add(v.getLong()));
		return out;
	}

	private static void exclusiveRecordIdRanges(Surreal surreal) {
		for (long i = 1; i <= 5; i++) {
			surreal.query("CREATE type::record('n', $i) SET v = 0", Collections.singletonMap("i", i));
		}
		final Id one = Id.from(1L);
		final Id four = Id.from(4L);
		assertEquals(Arrays.asList(1L, 2L, 3L, 4L), ids(surreal.select(new RecordIdRange("n", one, four))));
		assertEquals(Arrays.asList(2L, 3L), ids(surreal.select(new RecordIdRange("n", one, false, four, false))));
		assertEquals(Arrays.asList(1L, 2L, 3L), ids(surreal.select(new RecordIdRange("n", one, true, four, false))));
		assertEquals(Arrays.asList(2L, 3L, 4L, 5L),
				ids(surreal.select(new RecordIdRange("n", one, false, null, true))));

		// update and upsert honour the bounds too: only n:4 lies in 3>..=4.
		final RecordIdRange threeToFour = new RecordIdRange("n", Id.from(3L), false, four, true);
		surreal.update(threeToFour, UpType.MERGE, Collections.singletonMap("v", 1));
		surreal.upsert(threeToFour, UpType.MERGE, Collections.singletonMap("w", 1));
		final Value touched = surreal.query("SELECT VALUE id FROM n WHERE v = 1 AND w = 1").take(0);
		assertEquals(1, touched.getArray().len());
		assertEquals(4L, touched.getArray().get(0).getRecordId().getId().getLong());

		// delete n:1..n:5 exclusive at both ends leaves only n:1 and n:5.
		surreal.delete(new RecordIdRange("n", one, false, Id.from(5L), false));
		assertEquals(Arrays.asList(1L, 5L), ids(surreal.select(new RecordIdRange("n", null, null))));
	}

	@Test
	void exclusiveRecordIdRangesEmbedded() {
		withEmbedded(SetAndRangeBoundsTests::exclusiveRecordIdRanges);
	}

	@Test
	void exclusiveRecordIdRangesRemote() {
		withServer(SetAndRangeBoundsTests::exclusiveRecordIdRanges);
	}

	private static void rangeValueBounds(Surreal surreal) {
		final Response response = surreal.query("RETURN 1>..=5; RETURN 1..5; RETURN ..3; RETURN 2..");
		final Value excludedIncluded = response.take(0);
		assertTrue(excludedIncluded.isRange());
		assertEquals(1L, excludedIncluded.getRangeStart().get().getLong());
		assertFalse(excludedIncluded.isRangeStartInclusive());
		assertEquals(5L, excludedIncluded.getRangeEnd().get().getLong());
		assertTrue(excludedIncluded.isRangeEndInclusive());

		final Value includedExcluded = response.take(1);
		assertTrue(includedExcluded.isRangeStartInclusive());
		assertFalse(includedExcluded.isRangeEndInclusive());

		final Value noStart = response.take(2);
		assertFalse(noStart.getRangeStart().isPresent());
		assertFalse(noStart.isRangeStartInclusive());
		assertEquals(3L, noStart.getRangeEnd().get().getLong());

		final Value noEnd = response.take(3);
		assertFalse(noEnd.getRangeEnd().isPresent());
		assertFalse(noEnd.isRangeEndInclusive());
	}

	@Test
	void rangeValueBoundsEmbedded() {
		withEmbedded(SetAndRangeBoundsTests::rangeValueBounds);
	}

	@Test
	void rangeValueBoundsRemote() {
		withServer(SetAndRangeBoundsTests::rangeValueBounds);
	}

	public static class Tagged {
		public Set<Long> fromSet;
		public List<Long> setAsList;
		public Set<Long> fromArray;
		public SortedSet<Long> sorted;
		public TreeSet<Long> tree;
		public HashSet<Long> hash;

		public Tagged() {
		}
	}

	private static void sets(Surreal surreal) {
		// Reading a set.
		final Value read = surreal.query("RETURN <set>[3, 1, 3]").take(0);
		assertTrue(read.isSet());
		assertFalse(read.isArray());
		assertEquals(Arrays.asList(1L, 3L), longs(read.getSet()));

		// Writing one: duplicates collapse and the server sees a set.
		final ValueMut set = ValueMut.createSet(Arrays.asList(2L, 1L, 2L));
		final Response written = surreal.query("RETURN type::of($s); RETURN $s", Collections.singletonMap("s", set));
		assertEquals("set", written.take(0).getString());
		assertEquals(Arrays.asList(1L, 2L), longs(written.take(1).getSet()));

		// A set fits a set-typed field.
		surreal.query("DEFINE FIELD s ON typed TYPE set<int>");
		surreal.query("CREATE typed:1 SET s = $s",
				Collections.singletonMap("s", ValueMut.createSet(Arrays.asList(5L, 4L))));
		assertTrue(surreal.query("SELECT VALUE s FROM ONLY typed:1").take(0).isSet());

		// A java.util.Set binding is still an array, so array-typed fields keep
		// working.
		surreal.query("DEFINE FIELD a ON arrays TYPE array<int>");
		surreal.query("CREATE arrays:1 SET a = $a",
				Collections.singletonMap("a", new TreeSet<>(Arrays.asList(1L, 2L))));
		assertTrue(surreal.query("SELECT VALUE a FROM ONLY arrays:1").take(0).isArray());

		// POJO fields: sets convert to Set or List fields, and arrays to Set fields.
		final Tagged tagged = surreal
				.query("RETURN { fromSet: <set>[2, 1, 2], setAsList: <set>[3, 3], fromArray: [7, 7, 8], "
						+ "sorted: <set>[9, 8], tree: [6, 5], hash: [4, 4] }")
				.take(0).get(Tagged.class);
		assertEquals(new TreeSet<>(Arrays.asList(1L, 2L)), tagged.fromSet);
		assertEquals(Collections.singletonList(3L), tagged.setAsList);
		assertEquals(new TreeSet<>(Arrays.asList(7L, 8L)), tagged.fromArray);
		// Declared set types are honoured, including sorted ones.
		assertEquals(Arrays.asList(8L, 9L), new ArrayList<>(tagged.sorted));
		assertEquals(Arrays.asList(5L, 6L), new ArrayList<>(tagged.tree));
		assertEquals(Collections.singleton(4L), tagged.hash);
	}

	@Test
	void setsEmbedded() {
		withEmbedded(SetAndRangeBoundsTests::sets);
	}

	@Test
	void setsRemote() {
		withServer(SetAndRangeBoundsTests::sets);
	}

	public static class Nested {
		public List<Set<Long>> listOfSets;
		public Set<Set<Long>> setOfSets;
		public List<List<Long>> listOfLists;
		public List<List<Item>> listOfItemLists;

		public Nested() {
		}
	}

	public static class Item {
		public String name;

		public Item() {
		}
	}

	@Test
	void nestedCollectionsKeepDeclaredTypes() {
		withEmbedded(surreal -> {
			final Nested nested = surreal
					.query("RETURN { listOfSets: [<set>[2, 1, 2], <set>[3]], "
							+ "setOfSets: <set>[<set>[2, 1], <set>[3]], listOfLists: [[1, 2], [3]], "
							+ "listOfItemLists: [[{ name: 'a' }, { name: 'b' }], [{ name: 'c' }]] }")
					.take(0).get(Nested.class);
			assertEquals(2, nested.listOfSets.size());
			for (final java.lang.Object element : nested.listOfSets) {
				assertTrue(element instanceof Set, "List<Set<Long>> element is " + element.getClass());
			}
			assertEquals(new TreeSet<>(Arrays.asList(1L, 2L)), nested.listOfSets.get(0));
			assertEquals(Collections.singleton(3L), nested.listOfSets.get(1));

			assertEquals(2, nested.setOfSets.size());
			for (final java.lang.Object element : nested.setOfSets) {
				assertTrue(element instanceof Set, "Set<Set<Long>> element is " + element.getClass());
			}
			assertTrue(nested.setOfSets.contains(new TreeSet<>(Arrays.asList(1L, 2L))));
			assertTrue(nested.setOfSets.contains(Collections.singleton(3L)));

			assertEquals(Arrays.asList(Arrays.asList(1L, 2L), Collections.singletonList(3L)), nested.listOfLists);

			assertEquals(2, nested.listOfItemLists.size());
			assertEquals("a", nested.listOfItemLists.get(0).get(0).name);
			assertEquals("b", nested.listOfItemLists.get(0).get(1).name);
			assertEquals("c", nested.listOfItemLists.get(1).get(0).name);
		});
	}

	@Test
	void nestedSetsFromArrays() {
		withEmbedded(surreal -> {
			// Plain arrays convert to the declared Set types as well.
			final Nested nested = surreal.query("RETURN { listOfSets: [[2, 1, 2]], setOfSets: [[4, 4]] }").take(0)
					.get(Nested.class);
			assertEquals(Collections.singletonList(new TreeSet<>(Arrays.asList(1L, 2L))), nested.listOfSets);
			assertEquals(Collections.singleton(Collections.singleton(4L)), nested.setOfSets);
		});
	}

	@Test
	void createSetCopiesCallerValues() {
		withEmbedded(surreal -> {
			final ValueMut shared = ValueMut.createLong(7L);
			final ValueMut other = ValueMut.createLong(8L);
			// The same caller value twice is copied each time and collapses in the set.
			final ValueMut set = ValueMut.createSet(Arrays.asList(shared, shared, other));
			final Map<String, java.lang.Object> bindings = new HashMap<>();
			bindings.put("s", set);
			bindings.put("v", shared);
			final Response response = surreal.query("RETURN $s; RETURN $v", bindings);
			assertEquals(Arrays.asList(7L, 8L), longs(response.take(0).getSet()));
			// The caller's values were not consumed by the set.
			assertEquals(7L, response.take(1).getLong());
			final ValueMut again = ValueMut.createSet(Arrays.asList(shared, other));
			assertEquals("set",
					surreal.query("RETURN type::of($s)", Collections.singletonMap("s", again)).take(0).getString());
			assertEquals("7", shared.toString());
			assertEquals("8", other.toString());
		});
	}

	@Test
	void createSetRejectsUnsupportedElements() {
		withEmbedded(surreal -> {
			final ValueMut kept = ValueMut.createLong(1L);
			// The converted elements before the failing one are freed, and the caller's
			// value is untouched.
			assertThrows(SurrealException.class, () -> ValueMut.createSet(Arrays.asList(kept, 2L, BigInteger.ONE)));
			assertEquals("1", kept.toString());
			// Later calls still work.
			final ValueMut set = ValueMut.createSet(Arrays.asList(kept, 2L));
			assertEquals(Arrays.asList(1L, 2L),
					longs(surreal.query("RETURN $s", Collections.singletonMap("s", set)).take(0).getSet()));
		});
	}
}
