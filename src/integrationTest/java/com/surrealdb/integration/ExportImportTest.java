package com.surrealdb.integration;

import com.surrealdb.RecordId;
import com.surrealdb.Surreal;
import com.surrealdb.SurrealException;
import com.surrealdb.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class ExportImportTest {

    @TempDir
    Path tempDir;

    @Test
    void exportAndImport_roundTrip() throws Exception {
        Path exportFile = tempDir.resolve("backup.surql");

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.query("CREATE person:john SET name = 'John Doe'");
            surreal.query("CREATE person:jane SET name = 'Jane Doe'");
            surreal.export(exportFile);
        }

        assertTrue(Files.exists(exportFile));
        assertTrue(Files.size(exportFile) > 0);

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.importData(exportFile);

            Iterator<Value> people = surreal.select("person");
            int count = 0;
            while (people.hasNext()) {
                people.next();
                count++;
            }
            assertEquals(2, count);
        }
    }

    @Test
    void export_withStringPath() throws Exception {
        String exportPath = tempDir.resolve("string-path.surql").toString();

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.query("CREATE test:1 SET value = 42");
            surreal.export(exportPath);
        }

        assertTrue(new File(exportPath).exists());
    }

    @Test
    void export_withFile() throws Exception {
        File exportFile = tempDir.resolve("file-path.surql").toFile();

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.query("CREATE test:1 SET value = 42");
            surreal.export(exportFile);
        }

        assertTrue(exportFile.exists());
    }

    @Test
    void export_fluentApi() throws Exception {
        Path exportFile = tempDir.resolve("fluent.surql");

        try (Surreal surreal = new Surreal()) {
            Surreal result = surreal
                .connect("memory")
                .useNs("test")
                .useDb("test");
            result.query("CREATE test:1 SET value = 42");
            Surreal exportResult = result.export(exportFile);

            assertSame(result, exportResult);
        }

        assertTrue(Files.exists(exportFile));
    }

    @Test
    void importData_fluentApi() throws Exception {
        Path exportFile = tempDir.resolve("fluent-import.surql");

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.query("CREATE test:1 SET value = 42");
            surreal.export(exportFile);
        }

        try (Surreal surreal = new Surreal()) {
            Surreal connected = surreal.connect("memory").useNs("test").useDb("test");
            Surreal importResult = connected.importData(exportFile);

            assertSame(connected, importResult);
        }
    }

    @Test
    void import_nonexistentFile_throwsException() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");

            assertThrows(SurrealException.class, () -> {
                surreal.importData("/nonexistent/path/file.surql");
            });
        }
    }

    @Test
    void exportImport_preservesDataTypes() throws Exception {
        Path exportFile = tempDir.resolve("datatypes.surql");

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.query("CREATE test:1 SET " +
                "string = 'hello', " +
                "number = 42, " +
                "float = 3.14, " +
                "bool = true, " +
                "array = [1, 2, 3], " +
                "object = { nested: 'value' }");
            surreal.export(exportFile);
        }

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.importData(exportFile);

            Value result = surreal.select(new RecordId("test", 1))
                .orElseThrow(() -> new AssertionError("Expected record not found"));
            com.surrealdb.Object obj = result.getObject();
            assertEquals("hello", obj.get("string").getString());
            assertEquals(42, obj.get("number").getLong());
            assertTrue(obj.get("bool").getBoolean());
            assertTrue(obj.get("array").isArray());
            assertTrue(obj.get("object").isObject());
        }
    }

    @Test
    void exportImport_multipleTables() throws Exception {
        Path exportFile = tempDir.resolve("multi-table.surql");

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.query("CREATE user:1 SET name = 'Alice'");
            surreal.query("CREATE product:1 SET name = 'Widget'");
            surreal.query("CREATE order:1 SET user = user:1, product = product:1");
            surreal.export(exportFile);
        }

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.importData(exportFile);

            assertTrue(surreal.select(new RecordId("user", 1)).isPresent());
            assertTrue(surreal.select(new RecordId("product", 1)).isPresent());
            assertTrue(surreal.select(new RecordId("order", 1)).isPresent());
        }
    }

    @Test
    void exportImport_emptyDatabase() throws Exception {
        Path exportFile = tempDir.resolve("empty.surql");

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.export(exportFile);
        }

        assertTrue(Files.exists(exportFile));

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.importData(exportFile);
        }
    }

    @Test
    void exportImport_withSurrealKV() throws Exception {
        Path kvDir = tempDir.resolve("surrealkv");
        Path exportFile = tempDir.resolve("kv-backup.surql");

        try (Surreal surreal = new Surreal()) {
            surreal.connect("surrealkv://" + kvDir.toAbsolutePath())
                   .useNs("test").useDb("test");
            surreal.query("CREATE user:1 SET name = 'Test User'");
            surreal.export(exportFile);
        }

        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.importData(exportFile);

            assertTrue(surreal.select(new RecordId("user", 1)).isPresent());
        }
    }
}
