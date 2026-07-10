package com.surrealdb;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Surreal#exportSql(String, ExportOptions)},
 * {@link Surreal#exportSql(ExportOptions)} and {@link Surreal#exportSql()}.
 */
public class ExportOptionsTests {

	private static void createFixture(Surreal surreal) {
		surreal.connect("memory").useNs("test").useDb("test");
		surreal.query("CREATE person:tobie SET name = 'Tobie';");
		surreal.query("CREATE city:london SET name = 'London';");
		surreal.query("DEFINE FUNCTION fn::greet($name: string) { RETURN 'Hello, ' + $name; };");
		surreal.query("DEFINE PARAM $myparam VALUE 42;");
	}

	@Test
	void defaultExportContainsRecordsAndDefinitions() {
		try (Surreal surreal = new Surreal()) {
			createFixture(surreal);
			String export = surreal.exportSql();
			assertTrue(export.contains("DEFINE TABLE person"), export);
			assertTrue(export.contains("DEFINE TABLE city"), export);
			assertTrue(export.contains("person:tobie"), export);
			assertTrue(export.contains("city:london"), export);
			assertTrue(export.contains("DEFINE FUNCTION fn::greet"), export);
			assertTrue(export.contains("DEFINE PARAM $myparam"), export);
		}
	}

	@Test
	void recordsFalse_excludesRecordsButKeepsTableDefinitions() {
		try (Surreal surreal = new Surreal()) {
			createFixture(surreal);
			String export = surreal.exportSql(new ExportOptions().records(false));
			assertTrue(export.contains("DEFINE TABLE person"), export);
			assertTrue(export.contains("DEFINE TABLE city"), export);
			assertFalse(export.contains("person:tobie"), export);
			assertFalse(export.contains("city:london"), export);
		}
	}

	@Test
	void tablesFilter_limitsExportedTables() {
		try (Surreal surreal = new Surreal()) {
			createFixture(surreal);
			String export = surreal.exportSql(new ExportOptions().tables("person"));
			assertTrue(export.contains("DEFINE TABLE person"), export);
			assertTrue(export.contains("person:tobie"), export);
			assertFalse(export.contains("DEFINE TABLE city"), export);
			assertFalse(export.contains("city:london"), export);
		}
	}

	@Test
	void tablesEmptyVarargs_exportsAllTables() {
		try (Surreal surreal = new Surreal()) {
			createFixture(surreal);
			// Zero-arg tables() means "all tables", even after a narrower setting.
			String export = surreal.exportSql(new ExportOptions().tables("person").tables());
			assertTrue(export.contains("DEFINE TABLE person"), export);
			assertTrue(export.contains("DEFINE TABLE city"), export);
			assertTrue(export.contains("person:tobie"), export);
			assertTrue(export.contains("city:london"), export);
		}
	}

	@Test
	void nullOptions_throwNullPointerException() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			assertThrows(NullPointerException.class, () -> surreal.exportSql((ExportOptions) null));
			assertThrows(NullPointerException.class, () -> surreal.exportSql("unused.surql", (ExportOptions) null));
		}
	}

	@Test
	void tablesFalse_excludesAllTables() {
		try (Surreal surreal = new Surreal()) {
			createFixture(surreal);
			String export = surreal.exportSql(new ExportOptions().tables(false));
			assertFalse(export.contains("DEFINE TABLE"), export);
			assertFalse(export.contains("person:tobie"), export);
			assertTrue(export.contains("DEFINE FUNCTION fn::greet"), export);
		}
	}

	@Test
	void functionsAndParamsFalse_excludesDefinitions() {
		try (Surreal surreal = new Surreal()) {
			createFixture(surreal);
			String export = surreal.exportSql(new ExportOptions().functions(false).params(false));
			assertFalse(export.contains("DEFINE FUNCTION"), export);
			assertFalse(export.contains("DEFINE PARAM"), export);
			assertTrue(export.contains("DEFINE TABLE person"), export);
		}
	}

	@Test
	void stringExportEqualsFileExport() throws Exception {
		Path dir = Files.createTempDirectory("surrealdb_export_options");
		Path file = dir.resolve("export.surql");
		try (Surreal surreal = new Surreal()) {
			createFixture(surreal);
			ExportOptions options = new ExportOptions().records(false).tables("person");
			String fromString = surreal.exportSql(options);
			assertTrue(surreal.exportSql(file.toString(), options));
			String fromFile = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
			assertEquals(fromFile, fromString);
		} finally {
			Files.deleteIfExists(file);
			Files.deleteIfExists(dir);
		}
	}

	@Test
	void stringExportRoundTripViaImport() throws Exception {
		Path dir = Files.createTempDirectory("surrealdb_export_options_import");
		Path file = dir.resolve("roundtrip.surql");
		try {
			String export;
			try (Surreal surreal = new Surreal()) {
				createFixture(surreal);
				export = surreal.exportSql();
			}
			Files.write(file, export.getBytes(StandardCharsets.UTF_8));
			try (Surreal surreal = new Surreal()) {
				surreal.connect("memory").useNs("test").useDb("test");
				assertTrue(surreal.importSql(file.toString()));
				Response response = surreal.query("SELECT * FROM person");
				Array rows = response.take(0).getArray();
				assertEquals(1, rows.len());
				assertEquals("Tobie", rows.get(0).getObject().get("name").getString());
				Value greeting = surreal.query("RETURN fn::greet('Jaime')").take(0);
				assertEquals("Hello, Jaime", greeting.getString());
			}
		} finally {
			Files.deleteIfExists(file);
			Files.deleteIfExists(dir);
		}
	}
}
