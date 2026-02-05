package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Proves that use_ns must be called before use_db: calling use_db without a prior use_ns
 * causes "Cannot use database without namespace"; calling use_ns then use_db succeeds.
 */
public class UseNsUseDbTests {

    @Test
    void useNsThenUseDb_succeeds() {
        try (Surreal surreal = new Surreal()) {
            // Proves that calling use_ns first, then use_db, works: no exception and we can query.
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            Response response = surreal.query("INFO FOR DB");
            assertNotNull(response);
            assertTrue(response.size() >= 1);
        }
    }

    @Test
    void useDbWithoutUseNs_throws() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory");
            // Never call useNs — call useDb only
            SurrealException e = assertThrows(SurrealException.class, () -> {
                surreal.useDb("test_db");
            });
            assertNotNull(e.getMessage());
            assertTrue(
                    e.getMessage().toLowerCase().contains("namespace"),
                    "Expected message to mention namespace, got: " + e.getMessage()
            );
        }
    }
}
