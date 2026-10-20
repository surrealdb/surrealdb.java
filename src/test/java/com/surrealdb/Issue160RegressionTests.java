package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.surrealdb.signin.RootCredential;

/**
 * Regression coverage for
 * <a href="https://github.com/surrealdb/surrealdb.java/issues/160">#160</a>.
 * The reported bug: calling {@code useDb} + {@code health} in a loop on a
 * reused WebSocket connection ballooned the SDK's per-session replay log and
 * (on server 3.0.5) crashed the server worker. A fixed driver finishes the loop
 * quickly with stable per-iteration latency.
 * <p>
 * Skipped when no SurrealDB instance is reachable at
 * {@code ws://localhost:8000} so local {@code ./gradlew test} runs without a
 * server stay green; CI starts one before the test step.
 */
public class Issue160RegressionTests {

	private static final String WS_URL = "ws://localhost:8000";

	@Test
	@EnabledOnOs({OS.LINUX, OS.MAC})
	void useDbHealthLoop_doesNotDegrade() {
		final int iterations = 200;
		final long budgetMs = 10_000;
		try (Surreal surreal = new Surreal()) {
			try {
				surreal.connect(WS_URL);
				surreal.signin(new RootCredential("root", "root"));
			} catch (Exception e) {
				assumeTrue(false, "Skipping: no SurrealDB reachable at " + WS_URL + " (" + e.getMessage() + ")");
				return;
			}
			surreal.useNs("issue_160_ns");
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				surreal.useDb("issue_160_db");
				surreal.health();
			}
			long elapsed = System.currentTimeMillis() - start;
			assertTrue(elapsed < budgetMs,
					"useDb+health loop took " + elapsed + "ms (>" + budgetMs + "ms); replay log may be growing");
		}
	}
}
