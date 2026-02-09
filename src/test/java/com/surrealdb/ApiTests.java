package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Surreal#version()} and {@link Surreal#health()}.
 */
public class ApiTests {

    @Test
    void versionReturnsNonEmpty() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            String version = surreal.version();
            assertNotNull(version);
            assertFalse(version.isEmpty());
        }
    }

    @Test
    void healthReturnsTrueWhenConnected() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            boolean healthy = surreal.health();
            assertTrue(healthy);
        }
    }
}
