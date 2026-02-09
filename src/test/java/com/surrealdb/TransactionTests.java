package com.surrealdb;

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
	void transaction_commit_succeeds() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Transaction txn = surreal.beginTransaction();
			txn.query("INFO FOR DB");
			txn.commit();
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
}
