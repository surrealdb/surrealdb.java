package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.RawRecord;

/**
 * Converting a result to a record closes the native wrappers it reads along the
 * way, but not the ones passed to the record's components.
 */
public class RecordConversionCloseTests {

	@Test
	void nativeComponentsSurviveConversion() throws InterruptedException {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("CREATE raw:1 SET raw = { a: [1, 2] }, name = 'one', extra = 'ignored'").close();
			final RawRecord record = surreal.select(RawRecord.class, new RecordId("raw", 1L)).get();
			// Anything closed or collected by mistake is freed now, so a component
			// that lost its native data fails below instead of passing by luck.
			for (int i = 0; i < 3; i++) {
				System.gc();
				Thread.sleep(20);
			}
			assertEquals("raw", record.id().getTable());
			assertEquals("one", record.name());
			try (final Object raw = record.raw().getObject();
					final Value a = raw.get("a");
					final Array array = a.getArray()) {
				assertEquals(2, array.len());
			}
		}
	}
}
