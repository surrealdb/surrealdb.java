package com.surrealdb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.surrealdb.pojos.Person;

/**
 * The core client surface run against a real server over both remote protocols.
 * Most other suites use the embedded engine only; this one catches differences
 * in the WebSocket and HTTP engines and in the server version CI runs against
 * (see {@link TestServer}).
 */
public class ServerBackedTests {

	private static Surreal connect(String protocol) {
		return TestServer.connect("ws".equals(protocol) ? TestServer.WS_URL : TestServer.HTTP_URL);
	}

	private static <T> List<T> list(Iterator<T> it) {
		final List<T> out = new ArrayList<>();
		it.forEachRemaining(out::add);
		return out;
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws", "http"})
	void versionAndHealth(String protocol) {
		try (final Surreal surreal = connect(protocol)) {
			assertTrue(surreal.health());
			assertTrue(surreal.version().matches("\\d+\\.\\d+\\.\\d+.*"), surreal.version());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws", "http"})
	void recordCrud(String protocol) {
		try (final Surreal surreal = connect(protocol)) {
			final RecordId id = new RecordId("person", "tobie");
			final Person created = surreal.create(Person.class, id, Helpers.tobie);
			assertEquals(Helpers.tobie.name, created.name);

			final Optional<Person> selected = surreal.select(Person.class, id);
			assertTrue(selected.isPresent());
			assertEquals(Helpers.tobie.tags, selected.get().tags);

			final Person merged = surreal.update(Person.class, id, UpType.MERGE, Helpers.jaime);
			assertEquals(Helpers.jaime.name, merged.name);

			final Person upserted = surreal.upsert(Person.class, new RecordId("person", "emmanuel"), UpType.CONTENT,
					Helpers.emmanuel);
			assertEquals(Helpers.emmanuel.name, upserted.name);

			assertEquals(2, list(surreal.select("person")).size());
			surreal.delete(id);
			assertFalse(surreal.select(id).isPresent());
			surreal.delete("person");
			assertTrue(list(surreal.select("person")).isEmpty());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws", "http"})
	void insertAndRelate(String protocol) {
		try (final Surreal surreal = connect(protocol)) {
			final List<Value> inserted = surreal.insert("person", Helpers.tobie, Helpers.jaime);
			assertEquals(2, inserted.size());
			final RecordId a = inserted.get(0).getObject().get("id").getRecordId();
			final RecordId b = inserted.get(1).getObject().get("id").getRecordId();
			final Value edge = surreal.relate(a, "knows", b);
			assertEquals(a.toString(), edge.getObject().get("in").getRecordId().toString());
			final Value out = surreal
					.query("SELECT VALUE ->knows->person FROM ONLY $a", Collections.singletonMap("a", a)).take(0);
			assertEquals(1, out.getArray().len());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws", "http"})
	void queryBindingsAndErrors(String protocol) {
		try (final Surreal surreal = connect(protocol)) {
			final java.util.Map<String, java.lang.Object> params = new java.util.HashMap<>();
			params.put("a", 1);
			params.put("b", 2);
			params.put("s", "x");
			final Response response = surreal.query("RETURN $a + $b; RETURN string::uppercase($s)", params);
			assertEquals(3L, response.take(0).getLong());
			assertEquals("X", response.take(1).getString());
			assertThrows(SurrealException.class, () -> surreal.query("THROW 'boom'").take(0));
			assertThrows(SurrealException.class, () -> surreal.query("SELEC nonsense"));
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws", "http"})
	void runFunction(String protocol) {
		try (final Surreal surreal = connect(protocol)) {
			surreal.query("DEFINE FUNCTION fn::add($a: int, $b: int) { RETURN $a + $b; }");
			assertEquals(5L, surreal.run("fn::add", 2, 3).getLong());
			assertEquals(3L, surreal.run("string::len", "abc").getLong());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws"})
	void interactiveTransaction(String protocol) {
		try (final Surreal surreal = connect(protocol)) {
			final Transaction committed = surreal.beginTransaction();
			committed.query("CREATE item:1 SET v = 1");
			committed.commit();
			final Transaction cancelled = surreal.beginTransaction();
			cancelled.query("CREATE item:2 SET v = 2");
			cancelled.cancel();
			assertEquals(1, list(surreal.select("item")).size());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws"})
	void liveQuery(String protocol) throws Exception {
		try (final Surreal surreal = connect(protocol)) {
			surreal.query("DEFINE TABLE watched");
			try (final LiveStream stream = surreal.selectLive("watched")) {
				assertNotNull(stream.getQueryId());
				surreal.query("CREATE watched:1 SET v = 1");
				final Optional<LiveNotification> n = stream.next();
				assertTrue(n.isPresent());
				assertEquals("CREATE", n.get().getAction());
				assertEquals(stream.getQueryId(), n.get().getQueryId());
			}
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"http"})
	void exportAndImport(String protocol, @TempDir Path dir) throws Exception {
		try (final Surreal surreal = connect(protocol)) {
			surreal.query("CREATE thing:1 SET v = 1; CREATE thing:2 SET v = 2");
			final Path file = dir.resolve("export.surql");
			assertTrue(surreal.exportSql(file.toString()));
			assertTrue(Files.size(file) > 0);
			surreal.query("REMOVE TABLE thing");
			assertTrue(surreal.importSql(file.toString()));
			assertEquals(2, list(surreal.select("thing")).size());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws", "http"})
	void newSessionIsIsolated(String protocol) {
		try (final Surreal surreal = connect(protocol)) {
			surreal.query("CREATE marker:1");
			try (final Surreal other = surreal.newSession()) {
				other.useNs("java_tests").useDb("other_" + Long.toHexString(System.nanoTime()));
				other.query("DEFINE TABLE marker");
				assertTrue(list(other.select("marker")).isEmpty());
			}
			assertEquals(1, list(surreal.select("marker")).size());
		}
	}
}
