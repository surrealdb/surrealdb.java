package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Surreal#run(String, Object...)} (running a SurrealDB
 * function).
 */
public class RunTests {

	@Test
	void runNoArgFunction() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE FUNCTION fn::greet() { RETURN 'hello'; }");
			Value result = surreal.run("fn::greet");
			assertNotNull(result);
			assertTrue(result.isString());
			assertEquals("hello", result.getString());
		}
	}

	@Test
	void runSingleArgFunction() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE FUNCTION fn::concat($x:string) { RETURN $x + '!'; }");
			Value result = surreal.run("fn::concat", "hello");
			assertNotNull(result);
			assertTrue(result.isString());
			assertEquals("hello!", result.getString());
		}
	}

	@Test
	void runMultiArgFunction() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE FUNCTION fn::join($a:string, $b:string) { RETURN $a + '-' + $b; }");
			Value result = surreal.run("fn::join", "foo", "bar");
			assertNotNull(result);
			assertTrue(result.isString());
			assertEquals("foo-bar", result.getString());
		}
	}
}
