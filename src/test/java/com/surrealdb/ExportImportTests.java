package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tests for {@link Surreal#export(String)} and {@link Surreal#import_(String)}.
 */
public class ExportImportTests {

    @Test
    void exportAndImportRoundTrip() throws Exception {
        Path dir = Files.createTempDirectory("surrealdb_export_import");
        Path file = dir.resolve("backup.surql");
        try {
            try (Surreal surreal = new Surreal()) {
                surreal.connect("memory").useNs("test").useDb("test");
                surreal.query("CREATE person:id SET name = 'Alice';");
                boolean exported = surreal.export(file.toString());
                assertTrue(exported);
            }
            try (Surreal surreal = new Surreal()) {
                surreal.connect("memory").useNs("test").useDb("test");
                boolean imported = surreal.import_(file.toString());
                assertTrue(imported);
                Response response = surreal.query("SELECT * FROM person");
                Array rows = response.take(0).getArray();
                assertEquals(1, rows.len());
                assertEquals("Alice", rows.get(0).getObject().get("name").getString());
            }
        } finally {
            Files.deleteIfExists(file);
            Files.deleteIfExists(dir);
        }
    }
}
