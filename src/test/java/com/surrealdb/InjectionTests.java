package com.surrealdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.surrealdb.pojos.Person;

/**
 * Regression tests for SurrealQL injection through generated queries. Table
 * names and record contents passed to the CRUD helpers must never be parsed as
 * SurrealQL: table names are bound as table values and contents as variables.
 */
public class InjectionTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	/** A "table name" that would drop the victim table if it were interpolated. */
	private static final String HOSTILE = "victim; REMOVE TABLE victim; SELECT * FROM victim";

	private static void withEmbedded(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			test.accept(surreal);
		}
	}

	private static void withServer(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			try {
				surreal.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
			}
			surreal.useNs("test_ns").useDb("injection_" + System.nanoTime());
			test.accept(surreal);
		}
	}

	private static long count(Surreal surreal, String table) {
		final Value v = surreal
				.query("RETURN count(SELECT * FROM type::table($tb))", java.util.Collections.singletonMap("tb", table))
				.take(0);
		return v.getLong();
	}

	private static <T> List<T> toList(Iterator<T> it) {
		final List<T> out = new ArrayList<>();
		it.forEachRemaining(out::add);
		return out;
	}

	private static void hostileTableNames(Surreal surreal) {
		surreal.query("CREATE victim:1 SET name = 'safe'");
		final Person p = new Person();
		p.name = "mallory";

		// Every helper that takes a table name.
		assertEquals(1, surreal.create(HOSTILE, p).size());
		assertEquals(1, surreal.insert(HOSTILE, p).size());
		assertFalse(toList(surreal.select(HOSTILE)).isEmpty());
		assertFalse(toList(surreal.selectSync(HOSTILE)).isEmpty());
		assertFalse(toList(surreal.update(HOSTILE, UpType.MERGE, p)).isEmpty());
		assertFalse(toList(surreal.updateSync(HOSTILE, UpType.MERGE, p)).isEmpty());
		assertFalse(toList(surreal.update(new String[]{HOSTILE}, UpType.MERGE, p)).isEmpty());
		assertFalse(toList(surreal.upsert(HOSTILE, UpType.MERGE, p)).isEmpty());
		assertFalse(toList(surreal.upsertSync(new String[]{HOSTILE}, UpType.MERGE, p)).isEmpty());
		surreal.relate(new RecordId("victim", 1L), HOSTILE, new RecordId("victim", 1L));
		surreal.relate(new RecordId("victim", 1L), HOSTILE, new RecordId("victim", 1L), p);
		surreal.delete(HOSTILE);

		// The hostile string was a table name, so the victim table is intact.
		assertEquals(1, count(surreal, "victim"));
		assertEquals(0, count(surreal, HOSTILE));
	}

	@Test
	void hostileTableNamesEmbedded() {
		withEmbedded(InjectionTests::hostileTableNames);
	}

	@Test
	void hostileTableNamesRemote() {
		withServer(InjectionTests::hostileTableNames);
	}

	private static void assertRejectsRecordId(Executable call) {
		final SurrealException e = assertThrows(SurrealException.class, call);
		assertTrue(e.getMessage().contains("\"person:tobie\""), e.getMessage());
		assertTrue(e.getMessage().contains("RecordId"), e.getMessage());
	}

	private static void stringIsNeverARecordId(Surreal surreal) {
		surreal.query("CREATE person:tobie SET name = 'tobie'");
		final RecordId tobie = new RecordId("person", "tobie");
		final Person p = new Person();
		p.name = "changed";
		// "person:tobie" is a record id passed where a table name is expected.
		// Bound as a table it would create a table called `person:tobie` or
		// silently delete nothing, so every helper rejects it before sending.
		final String id = "person:tobie";
		assertRejectsRecordId(() -> surreal.create(id, p));
		assertRejectsRecordId(() -> surreal.insert(id, p));
		assertRejectsRecordId(() -> surreal.insertRelation(id, new InsertRelation(Id.from(1), tobie, tobie)));
		assertRejectsRecordId(() -> surreal.relate(tobie, id, tobie));
		assertRejectsRecordId(() -> surreal.relate(tobie, id, tobie, p));
		assertRejectsRecordId(() -> surreal.select(id));
		assertRejectsRecordId(() -> surreal.selectSync(id));
		assertRejectsRecordId(() -> surreal.update(id, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.updateSync(id, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.update(new String[]{"person", id}, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.updateSync(new String[]{"person", id}, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.upsert(id, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.upsertSync(id, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.upsert(new String[]{"person", id}, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.upsertSync(new String[]{"person", id}, UpType.MERGE, p));
		assertRejectsRecordId(() -> surreal.delete(id));
		// Nothing was written: no stray `person:tobie` table, and the record is
		// unchanged.
		final String tables = surreal.query("INFO FOR DB").take(0).getObject().get("tables").toString();
		assertFalse(tables.contains(id), tables);
		assertEquals("tobie", surreal.select(tobie).get().getObject().get("name").getString());
		assertEquals(1, count(surreal, "person"));
	}

	@Test
	void stringIsNeverARecordIdEmbedded() {
		withEmbedded(InjectionTests::stringIsNeverARecordId);
	}

	@Test
	void stringIsNeverARecordIdRemote() {
		withServer(InjectionTests::stringIsNeverARecordId);
	}

	private static void contentIsBound(Surreal surreal) {
		// A string that would close the object literal and run a second
		// statement if contents were rendered with to_sql().
		final String payload = "x' }; REMOVE TABLE victim; CREATE victim CONTENT { name: 'y";
		surreal.query("CREATE victim:1");
		final Person p = new Person();
		p.name = payload;
		final Value inserted = surreal.insert("person", p).get(0);
		assertEquals(payload, inserted.getObject().get("name").getString());
		final Value created = surreal.create(new RecordId("person", "id"), p);
		assertEquals(payload, created.getObject().get("name").getString());
		final Value updated = surreal.update(new RecordId("person", "id"), UpType.CONTENT, p);
		assertEquals(payload, updated.getObject().get("name").getString());
		assertTrue(surreal.select(new RecordId("person", "id")).isPresent());
		assertEquals(1, count(surreal, "victim"));
	}

	@Test
	void contentIsBoundEmbedded() {
		withEmbedded(InjectionTests::contentIsBound);
	}

	@Test
	void contentIsBoundRemote() {
		withServer(InjectionTests::contentIsBound);
	}

	@Test
	void runRejectsNonFunctionNames() {
		withEmbedded(surreal -> {
			surreal.query("CREATE victim:1");
			assertThrows(SurrealException.class, () -> surreal.run("string::len('a'); REMOVE TABLE victim; RETURN fn"));
			assertThrows(SurrealException.class, () -> surreal.run(""));
			assertThrows(SurrealException.class, () -> surreal.run("string::"));
			assertEquals(3L, surreal.run("string::len", "abc").getLong());
			assertEquals(1, count(surreal, "victim"));
		});
	}
}
