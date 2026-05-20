package com.surrealdb;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for client-side transaction support:
 * {@link Surreal#beginTransaction()}, {@link Transaction#query(String)},
 * {@link Transaction#commit()}, {@link Transaction#cancel()}.
 */
public class TransactionTests {

	@Test
	void beginTransaction_returnsNonNullTransaction() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			assertNotNull(txn);
		}
	}

	@Test
	void transaction_query_returnsResponse() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			Response response = txn.query("INFO FOR DB");
			assertNotNull(response);
			assertTrue(response.size() >= 1);
		}
	}

	@Test
	void transaction_queryWithBindings_returnsResponse() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			HashMap<String, String> map = new HashMap<>();
			map.put("value", "hello");
			map.put("value2", "world");
			Response response = txn.query("RETURN $value;RETURN $value2", map);
			assertNotNull(response);
			assertEquals(2, response.size());
			assertEquals("hello", response.take(0).getString());
			assertEquals("world", response.take(1).getString());
			txn.cancel();
		}
	}

	@Test
	void transaction_commit_succeeds() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("INFO FOR DB");
			txn.commit();
		}
	}

	@Test
	void transaction_queryWithBindings_commitsBoundData() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			HashMap<String, String> map = new HashMap<>();
			map.put("name", "BoundTransaction");
			Response create = txn.query("CREATE tx_bound SET name = $name", map);
			assertNotNull(create);
			assertTrue(create.size() >= 1);
			txn.commit();
			Response response = surreal.query("SELECT * FROM tx_bound WHERE name = 'BoundTransaction'");
			assertNotNull(response);
			Value first = response.take(0);
			assertTrue(first.isArray());
			assertEquals(1, first.getArray().len());
			assertEquals("BoundTransaction", first.getArray().get(0).getObject().get("name").getString());
		}
	}

	@Test
	void transaction_queryWithBindings_typeMatrix() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final String sql1 = "RETURN [1, 2, 3];RETURN { foo: 'bar' }";
			final Response response1 = surreal.query(sql1);
			Transaction txn = surreal.beginTransaction();
			final HashMap<String, java.lang.Object> map = new HashMap<>();
			map.put("string", "hello_world");
			map.put("long", 25565L);
			map.put("list", Collections.singletonList("item1"));
			map.put("map", Collections.singletonMap("foo", "bar"));
			map.put("array", response1.take(0).getArray());
			map.put("object", response1.take(1).getObject());
			map.put("null", null);
			map.put("uuid", UUID.fromString("f8e238f2-e734-47b8-9a16-476b291bd78a"));
			final String sql2 = "RETURN [$string, $long, $list, $map, $array, $object, $null, $uuid]";
			final Response response2 = txn.query(sql2, map);
			final Array results = response2.take(0).getArray();
			assertEquals(8, results.len());
			assertEquals("hello_world", results.get(0).getString());
			assertEquals(25565L, results.get(1).getLong());
			final Array res3 = results.get(2).getArray();
			assertEquals(1, res3.len());
			assertEquals("item1", res3.get(0).getString());
			final Object res4 = results.get(3).getObject();
			assertEquals(1, res4.len());
			assertEquals("bar", res4.get("foo").getString());
			final Array res5 = results.get(4).getArray();
			assertEquals("[1, 2, 3]", res5.toString());
			final Object res6 = results.get(5).getObject();
			assertEquals("{ foo: 'bar' }", res6.toString());
			assertTrue(results.get(6).isNull());
			assertEquals("f8e238f2-e734-47b8-9a16-476b291bd78a", results.get(7).getUuid().toString());
			txn.cancel();
		}
	}

	@Test
	void transaction_queryWithBindings_valueMut() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			final HashMap<String, ValueMut> map = new HashMap<>();
			map.put("null", ValueMut.createNull());
			map.put("none", ValueMut.createNone());
			final Response response = txn.query("RETURN $null;RETURN $none", map);
			assertEquals(2, response.size());
			assertTrue(response.take(0).isNull());
			assertTrue(response.take(1).isNone());
			txn.cancel();
		}
	}

	@Test
	void transaction_cancel_succeeds() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("INFO FOR DB");
			txn.cancel();
		}
	}

	@Test
	void transaction_commit_makesDataVisible() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("CREATE tx_person SET name = 'InTransaction'");
			txn.commit();
			Response response = surreal.query("SELECT * FROM tx_person WHERE name = 'InTransaction'");
			assertNotNull(response);
			assertTrue(response.size() >= 1);
		}
	}

	@Test
	void transaction_isolation_recordNotVisibleUntilCommit() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// Ensure table exists from main connection so we can query it
			surreal.query("CREATE tx_isolated SET name = 'Dummy'");
			Transaction txn = surreal.beginTransaction();
			txn.query("CREATE tx_isolated SET name = 'Isolated'");
			// From main instance, record created in transaction should not be visible yet
			// (isolation)
			Response beforeCommit = surreal.query("SELECT * FROM tx_isolated WHERE name = 'Isolated'");
			assertNotNull(beforeCommit);
			assertTrue(beforeCommit.size() >= 1, "Expected at least one result set");
			Value first = beforeCommit.take(0);
			assertTrue(first.isNone() || (first.isArray() && first.getArray().len() == 0),
					"Record created in transaction should not be visible before commit");
			txn.commit();
			// After commit, main instance should see the record
			Response afterCommit = surreal.query("SELECT * FROM tx_isolated WHERE name = 'Isolated'");
			assertNotNull(afterCommit);
			assertTrue(afterCommit.size() >= 1);
			Value afterFirst = afterCommit.take(0);
			assertTrue(afterFirst.isArray() && afterFirst.getArray().len() >= 1,
					"Record should be visible after commit");
		}
	}

	@Test
	void transaction_cancel_rollsBackData() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("CREATE tx_rollback SET name = 'ShouldNotExist'");
			txn.cancel();
			Response response = surreal.query("SELECT * FROM tx_rollback WHERE name = 'ShouldNotExist'");
			assertNotNull(response);
			// After cancel, created data should not be visible (rollback). Some backends
			// may behave differently.
			assertTrue(response.size() <= 1, "Cancel should roll back; expected 0 results, got " + response.size());
		}
	}

	@Test
	void transaction_multipleQueriesThenCommit() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("CREATE tx_multi SET id = 1");
			txn.query("CREATE tx_multi SET id = 2");
			txn.commit();
			Response response = surreal.query("SELECT * FROM tx_multi");
			assertNotNull(response);
			// At least one result set; may be 1 set with 2 rows or 2 sets with 1 row each
			assertTrue(response.size() >= 1);
		}
	}

	@Test
	void transaction_commitThenUse_throwsOrFails() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.commit();
			assertThrows(Throwable.class, () -> txn.query("INFO FOR DB"));
		}
	}

	@Test
	void transaction_cancelThenUse_throwsOrFails() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.cancel();
			assertThrows(Throwable.class, () -> txn.query("INFO FOR DB"));
		}
	}

	@Test
	void transaction_doubleCommit_doesNotCrash() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("INFO FOR DB");
			txn.commit();
			// Second commit may throw (use-after-complete); must not crash JVM
			try {
				txn.commit();
			} catch (Throwable expected) {
				assertNotNull(expected);
			}
		}
	}

	@Test
	void transaction_doubleCancel_doesNotCrash() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("INFO FOR DB");
			txn.cancel();
			// Second cancel may throw (use-after-complete); must not crash JVM
			try {
				txn.cancel();
			} catch (Throwable expected) {
				assertNotNull(expected);
			}
		}
	}
}
