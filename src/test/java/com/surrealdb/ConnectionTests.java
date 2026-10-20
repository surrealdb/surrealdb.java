package com.surrealdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.surrealdb.pojos.Person;

public class ConnectionTests {

	@Test
	void connectWebsocket() throws Exception {
		try (Surreal surreal = new Surreal()) {
			ExecutorService executor = Executors.newSingleThreadExecutor();
			try {
				Future<Void> future = executor.submit(new Callable<Void>() {
					@Override
					public Void call() throws SurrealException {
						surreal.connect("ws://localhost:8000").useNs("test").useDb("test");
						assertNotNull(surreal.version());
						return null;
					}
				});
				future.get(10, TimeUnit.SECONDS);
			} catch (TimeoutException e) {
				fail("connect() blocked for 10s (is a SurrealDB server running at ws://localhost:8000?)");
			} finally {
				executor.shutdownNow();
				executor.awaitTermination(2, TimeUnit.SECONDS);
			}
		}
	}

	@Test
	void connectSurrealKV() throws SurrealException, IOException {
		final Path tempDir = Files.createTempDirectory("surrealkv");
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("surrealkv://" + tempDir.toAbsolutePath()).useNs("test").useDb("test");
			final Person created = surreal.create(Person.class, "person", Helpers.tobie).get(0);
			assertNotNull(created.id);
		}
	}

	@Test
	void connectMemory() throws SurrealException {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			{
				final Response response = surreal.query("INFO FOR ROOT");
				final Value value = response.take(0);
				assertTrue(value.isObject());
				final Object object = value.getObject();
				assertEquals(object.len(), 7);
				{
					final Value v = object.get("accesses");
					assertTrue(v.isObject());
					assertEquals(v.getObject().len(), 0);
					assertEquals("{  }", v.toString());
				}
				{
					final Value v = object.get("config");
					assertTrue(v.isObject());
				}
				{
					final Value v = object.get("defaults");
					assertTrue(v.isObject());
				}
				{
					final Value v = object.get("namespaces");
					assertTrue(v.isObject());
					assertEquals("{ test_ns: 'DEFINE NAMESPACE test_ns' }", v.toString());
					assertEquals(v.getObject().len(), 1);
				}
				{
					final Value v = object.get("users");
					assertTrue(v.isObject());
					assertEquals(v.getObject().len(), 0);
					assertEquals("{  }", v.toString());
				}
				{
					final Value v = object.get("nodes");
					assertTrue(v.isObject());
					assertEquals(v.getObject().len(), 1);
				}
				{
					final Value v = object.get("system");
					assertTrue(v.isObject());
				}
			}
		}
	}
}
