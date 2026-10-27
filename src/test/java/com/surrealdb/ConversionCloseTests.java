package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

/**
 * Converting a result to a POJO closes the native wrappers it reads along the
 * way, but not the ones that become fields of the POJO.
 */
public class ConversionCloseTests {

	public static class Thing {
		public RecordId id;
		public Value raw;
		public Id ref;
		public RecordId link;
		public Geometry location;
		public String name;
		public List<Long> nums;
		public Map<String, String> tags;
		public Inner inner;
	}

	public static class Inner {
		public Value payload;
		public long n;
	}

	private static Surreal connect() {
		final Surreal surreal = new Surreal();
		surreal.connect("memory").useNs("test_ns").useDb("test_db");
		surreal.query("CREATE thing:1 SET raw = { a: 1 }, ref = other:42, link = other:43, location = (1.5, 2.5),"
				+ " name = 'one', nums = [1, 2], tags = { k: 'v' }, inner = { payload: [7, 8], n: 3 };"
				+ " CREATE thing:2 SET name = 'two'").close();
		return surreal;
	}

	private static void collectGarbage() throws InterruptedException {
		// Anything closed or collected by mistake is freed now, so a field that lost
		// its native data fails below instead of passing by luck.
		for (int i = 0; i < 3; i++) {
			System.gc();
			Thread.sleep(20);
		}
	}

	private static void assertFieldsUsable(Thing thing) {
		assertEquals("thing", thing.id.getTable());
		try (final Object raw = thing.raw.getObject(); final Value a = raw.get("a")) {
			assertEquals(1L, a.getLong());
		}
		assertEquals(42L, thing.ref.getLong());
		assertEquals("other", thing.link.getTable());
		assertEquals(new Point2D.Double(1.5, 2.5), thing.location.getPoint());
		assertEquals("one", thing.name);
		assertEquals(Arrays.asList(1L, 2L), thing.nums);
		assertEquals(Collections.singletonMap("k", "v"), thing.tags);
		assertEquals(3L, thing.inner.n);
		try (final Array payload = thing.inner.payload.getArray()) {
			assertEquals(2, payload.len());
		}
	}

	@Test
	void nativeFieldsSurviveConversion() throws InterruptedException {
		try (final Surreal surreal = connect()) {
			final Thing thing = surreal.select(Thing.class, new RecordId("thing", 1L)).get();
			collectGarbage();
			assertFieldsUsable(thing);
		}
	}

	@Test
	void valueGetLeavesTheCallersValueOpen() throws InterruptedException {
		try (final Surreal surreal = connect();
				final Response response = surreal.query("SELECT * FROM thing:1");
				final Value rows = response.take(0);
				final Array array = rows.getArray();
				final Value row = array.get(0)) {
			final Thing thing = row.get(Thing.class);
			collectGarbage();
			assertFieldsUsable(thing);
			// The caller's own value is still theirs to use.
			assertTrue(row.isObject());
		}
	}

	@Test
	void convertingIteratorReleasesItselfWhenExhausted() throws InterruptedException {
		try (final Surreal surreal = connect()) {
			final Iterator<Thing> things = surreal.select(Thing.class, "thing");
			Thing first = null;
			int count = 0;
			while (things.hasNext()) {
				final Thing thing = things.next();
				if (first == null || thing.name.equals("one")) {
					first = thing;
				}
				count++;
			}
			assertEquals(2, count);
			assertFalse(things.hasNext());
			assertThrows(NoSuchElementException.class, things::next);
			collectGarbage();
			assertFieldsUsable(first);
		}
	}
}
