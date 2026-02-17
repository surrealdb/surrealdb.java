package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the structured error handling classes.
 * These tests construct exceptions directly (no JNI) to verify the Java-side
 * class hierarchy, detail parsing, convenience getters, and cause chain traversal.
 */
public class ErrorTests {

	// ---- ErrorKind constants ----

	@Test
	void errorKindConstants() {
		assertEquals("Validation", ErrorKind.VALIDATION);
		assertEquals("Configuration", ErrorKind.CONFIGURATION);
		assertEquals("Thrown", ErrorKind.THROWN);
		assertEquals("Query", ErrorKind.QUERY);
		assertEquals("Serialization", ErrorKind.SERIALIZATION);
		assertEquals("NotAllowed", ErrorKind.NOT_ALLOWED);
		assertEquals("NotFound", ErrorKind.NOT_FOUND);
		assertEquals("AlreadyExists", ErrorKind.ALREADY_EXISTS);
		assertEquals("Connection", ErrorKind.CONNECTION);
		assertEquals("Internal", ErrorKind.INTERNAL);
	}

	// ---- Exception hierarchy ----

	@Test
	void hierarchyValidation() {
		ValidationException e = new ValidationException("parse error", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertTrue(e instanceof SurrealException);
		assertTrue(e instanceof RuntimeException);
		assertEquals(ErrorKind.VALIDATION, e.getKind());
	}

	@Test
	void hierarchyConfiguration() {
		ConfigurationException e = new ConfigurationException("not supported", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.CONFIGURATION, e.getKind());
	}

	@Test
	void hierarchyThrown() {
		ThrownException e = new ThrownException("user error", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.THROWN, e.getKind());
	}

	@Test
	void hierarchyQuery() {
		QueryException e = new QueryException("timeout", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.QUERY, e.getKind());
	}

	@Test
	void hierarchySerialization() {
		SerializationException e = new SerializationException("bad data", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.SERIALIZATION, e.getKind());
	}

	@Test
	void hierarchyNotAllowed() {
		NotAllowedException e = new NotAllowedException("denied", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.NOT_ALLOWED, e.getKind());
	}

	@Test
	void hierarchyNotFound() {
		NotFoundException e = new NotFoundException("missing", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.NOT_FOUND, e.getKind());
	}

	@Test
	void hierarchyAlreadyExists() {
		AlreadyExistsException e = new AlreadyExistsException("duplicate", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.ALREADY_EXISTS, e.getKind());
	}

	@Test
	void hierarchyInternal() {
		InternalException e = new InternalException("unexpected", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.INTERNAL, e.getKind());
	}

	@Test
	void hierarchyServerException() {
		ServerException e = new ServerException(ErrorKind.CONNECTION, "conn error", (java.lang.Object) null, null);
		assertTrue(e instanceof SurrealException);
		assertFalse(e instanceof InternalException);
		assertEquals(ErrorKind.CONNECTION, e.getKind());
	}

	@Test
	void unknownKindProducesBaseServerException() {
		// Unknown kinds produce ServerException, NOT InternalException
		ServerException e = new ServerException("FutureKind", "future error", (java.lang.Object) null, null);
		assertEquals("FutureKind", e.getKind());
		assertFalse(e instanceof InternalException);
	}

	@Test
	void catchAllWithSurrealException() {
		// Single SurrealException catches both server and SDK errors
		try {
			throw new NotAllowedException("denied", (java.lang.Object) null, null);
		} catch (SurrealException e) {
			assertTrue(e instanceof ServerException);
		}

		try {
			throw new SurrealException("sdk error");
		} catch (SurrealException e) {
			assertFalse(e instanceof ServerException);
		}
	}

	// ---- Detail parsing (JSON -> Object) ----

	@Test
	void detailParserNull() {
		assertNull(DetailParser.parseDetailsJson(null));
		assertNull(DetailParser.parseDetailsJson(""));
	}

	@Test
	void detailParserString() {
		java.lang.Object result = DetailParser.parseDetailsJson("\"Parse\"");
		assertEquals("Parse", result);
	}

	@Test
	void detailParserUnitObject() {
		// {"Auth": "TokenExpired"}
		java.lang.Object result = DetailParser.parseDetailsJson("{\"Auth\": \"TokenExpired\"}");
		assertTrue(result instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, java.lang.Object> map = (Map<String, java.lang.Object>) result;
		assertEquals("TokenExpired", map.get("Auth"));
	}

	@Test
	void detailParserStructObject() {
		// {"Table": {"name": "users"}}
		java.lang.Object result = DetailParser.parseDetailsJson("{\"Table\": {\"name\": \"users\"}}");
		assertTrue(result instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, java.lang.Object> map = (Map<String, java.lang.Object>) result;
		assertTrue(map.get("Table") instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, java.lang.Object> inner = (Map<String, java.lang.Object>) map.get("Table");
		assertEquals("users", inner.get("name"));
	}

	@Test
	void detailParserNumber() {
		java.lang.Object result = DetailParser.parseDetailsJson("{\"TimedOut\": {\"duration\": {\"secs\": 30, \"nanos\": 0}}}");
		assertTrue(result instanceof Map);
		@SuppressWarnings("unchecked")
		Map<String, java.lang.Object> map = (Map<String, java.lang.Object>) result;
		@SuppressWarnings("unchecked")
		Map<String, java.lang.Object> timedOut = (Map<String, java.lang.Object>) map.get("TimedOut");
		@SuppressWarnings("unchecked")
		Map<String, java.lang.Object> duration = (Map<String, java.lang.Object>) timedOut.get("duration");
		assertEquals(30, duration.get("secs"));
		assertEquals(0, duration.get("nanos"));
	}

	@Test
	void detailParserNullLiteral() {
		java.lang.Object result = DetailParser.parseDetailsJson("null");
		assertNull(result);
	}

	// ---- hasDetailKey / getDetailValue ----

	@Test
	void hasDetailKeyString() {
		assertTrue(ServerException.hasDetailKey("Parse", "Parse"));
		assertFalse(ServerException.hasDetailKey("Parse", "Other"));
	}

	@Test
	void hasDetailKeyMap() {
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", new HashMap<>());
		assertTrue(ServerException.hasDetailKey(details, "Table"));
		assertFalse(ServerException.hasDetailKey(details, "Record"));
	}

	@Test
	void hasDetailKeyNull() {
		assertFalse(ServerException.hasDetailKey(null, "Parse"));
	}

	@Test
	void getDetailValueString() {
		// Unit variant: key is present but has no value
		assertNull(ServerException.getDetailValue("Parse", "Parse"));
	}

	@Test
	void getDetailValueMap() {
		Map<String, java.lang.Object> inner = new HashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", inner);
		assertEquals(inner, ServerException.getDetailValue(details, "Table"));
		assertNull(ServerException.getDetailValue(details, "Other"));
	}

	@Test
	void getNestedStringHelper() {
		Map<String, java.lang.Object> inner = new HashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", inner);
		assertEquals("users", ServerException.getNestedString(details, "Table", "name"));
		assertNull(ServerException.getNestedString(details, "Table", "missing"));
		assertNull(ServerException.getNestedString(details, "Other", "name"));
	}

	@Test
	void isNewtypeValueHelper() {
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Auth", "TokenExpired");
		assertTrue(ServerException.isNewtypeValue(details, "Auth", "TokenExpired"));
		assertFalse(ServerException.isNewtypeValue(details, "Auth", "InvalidAuth"));
		assertFalse(ServerException.isNewtypeValue(details, "Other", "TokenExpired"));
	}

	// ---- Convenience getters on subclasses ----

	@Test
	void validationParseError() {
		ValidationException e = new ValidationException("parse error", "\"Parse\"", null);
		assertTrue(e.isParseError());
		assertNull(e.getParameterName());
	}

	@Test
	void validationInvalidParameter() {
		ValidationException e = new ValidationException("bad param",
				"{\"InvalidParameter\": {\"name\": \"foo\"}}", null);
		assertFalse(e.isParseError());
		assertEquals("foo", e.getParameterName());
	}

	@Test
	void configurationLiveQueryNotSupported() {
		ConfigurationException e = new ConfigurationException("not supported",
				"\"LiveQueryNotSupported\"", null);
		assertTrue(e.isLiveQueryNotSupported());
	}

	@Test
	void queryNotExecuted() {
		QueryException e = new QueryException("not executed", "\"NotExecuted\"", null);
		assertTrue(e.isNotExecuted());
		assertFalse(e.isTimedOut());
		assertFalse(e.isCancelled());
		assertNull(e.getTimeout());
	}

	@Test
	void queryTimedOut() {
		QueryException e = new QueryException("timed out",
				"{\"TimedOut\": {\"duration\": {\"secs\": 30, \"nanos\": 500}}}", null);
		assertTrue(e.isTimedOut());
		assertFalse(e.isNotExecuted());
		Map<String, java.lang.Object> timeout = e.getTimeout();
		assertNotNull(timeout);
		assertEquals(30, timeout.get("secs"));
		assertEquals(500, timeout.get("nanos"));
	}

	@Test
	void queryCancelled() {
		QueryException e = new QueryException("cancelled", "\"Cancelled\"", null);
		assertTrue(e.isCancelled());
		assertFalse(e.isTimedOut());
	}

	@Test
	void serializationDeserialization() {
		SerializationException e = new SerializationException("deser error",
				"\"Deserialization\"", null);
		assertTrue(e.isDeserialization());
	}

	@Test
	void serializationSerialization() {
		SerializationException e = new SerializationException("ser error",
				"\"Serialization\"", null);
		assertFalse(e.isDeserialization());
	}

	@Test
	void notAllowedTokenExpired() {
		NotAllowedException e = new NotAllowedException("token expired",
				"{\"Auth\": \"TokenExpired\"}", null);
		assertTrue(e.isTokenExpired());
		assertFalse(e.isInvalidAuth());
		assertFalse(e.isScriptingBlocked());
		assertNull(e.getMethodName());
		assertNull(e.getFunctionName());
	}

	@Test
	void notAllowedInvalidAuth() {
		NotAllowedException e = new NotAllowedException("invalid auth",
				"{\"Auth\": \"InvalidAuth\"}", null);
		assertTrue(e.isInvalidAuth());
		assertFalse(e.isTokenExpired());
	}

	@Test
	void notAllowedScripting() {
		NotAllowedException e = new NotAllowedException("scripting blocked",
				"\"Scripting\"", null);
		assertTrue(e.isScriptingBlocked());
		assertFalse(e.isTokenExpired());
	}

	@Test
	void notAllowedMethod() {
		NotAllowedException e = new NotAllowedException("method blocked",
				"{\"Method\": {\"name\": \"query\"}}", null);
		assertEquals("query", e.getMethodName());
		assertNull(e.getFunctionName());
	}

	@Test
	void notAllowedFunction() {
		NotAllowedException e = new NotAllowedException("fn blocked",
				"{\"Function\": {\"name\": \"http::get\"}}", null);
		assertEquals("http::get", e.getFunctionName());
		assertNull(e.getMethodName());
	}

	@Test
	void notFoundTable() {
		NotFoundException e = new NotFoundException("table not found",
				"{\"Table\": {\"name\": \"users\"}}", null);
		assertEquals("users", e.getTableName());
		assertNull(e.getRecordId());
		assertNull(e.getMethodName());
		assertNull(e.getNamespaceName());
		assertNull(e.getDatabaseName());
	}

	@Test
	void notFoundRecord() {
		NotFoundException e = new NotFoundException("record not found",
				"{\"Record\": {\"id\": \"person:1\"}}", null);
		assertEquals("person:1", e.getRecordId());
		assertNull(e.getTableName());
	}

	@Test
	void notFoundMethod() {
		NotFoundException e = new NotFoundException("method not found",
				"{\"Method\": {\"name\": \"unknown\"}}", null);
		assertEquals("unknown", e.getMethodName());
	}

	@Test
	void notFoundNamespace() {
		NotFoundException e = new NotFoundException("ns not found",
				"{\"Namespace\": {\"name\": \"test_ns\"}}", null);
		assertEquals("test_ns", e.getNamespaceName());
	}

	@Test
	void notFoundDatabase() {
		NotFoundException e = new NotFoundException("db not found",
				"{\"Database\": {\"name\": \"test_db\"}}", null);
		assertEquals("test_db", e.getDatabaseName());
	}

	@Test
	void alreadyExistsRecord() {
		AlreadyExistsException e = new AlreadyExistsException("duplicate record",
				"{\"Record\": {\"id\": \"person:1\"}}", null);
		assertEquals("person:1", e.getRecordId());
		assertNull(e.getTableName());
	}

	@Test
	void alreadyExistsTable() {
		AlreadyExistsException e = new AlreadyExistsException("duplicate table",
				"{\"Table\": {\"name\": \"users\"}}", null);
		assertEquals("users", e.getTableName());
		assertNull(e.getRecordId());
	}

	// ---- Cause chain ----

	@Test
	void causeChainStandard() {
		InternalException root = new InternalException("root", (java.lang.Object) null, null);
		NotAllowedException middle = new NotAllowedException("middle",
				"{\"Auth\": \"TokenExpired\"}", root);
		NotFoundException top = new NotFoundException("top",
				"{\"Table\": {\"name\": \"users\"}}", middle);

		// Standard Java cause chain
		assertEquals(middle, top.getCause());
		assertEquals(root, middle.getCause());
		assertNull(root.getCause());

		// Typed server cause
		assertEquals(middle, top.getServerCause());
		assertEquals(root, middle.getServerCause());
		assertNull(root.getServerCause());
	}

	@Test
	void hasKindTraversal() {
		InternalException root = new InternalException("root", (java.lang.Object) null, null);
		NotAllowedException middle = new NotAllowedException("middle", (java.lang.Object) null, root);
		NotFoundException top = new NotFoundException("top", (java.lang.Object) null, middle);

		assertTrue(top.hasKind(ErrorKind.NOT_FOUND));
		assertTrue(top.hasKind(ErrorKind.NOT_ALLOWED));
		assertTrue(top.hasKind(ErrorKind.INTERNAL));
		assertFalse(top.hasKind(ErrorKind.QUERY));
	}

	@Test
	void findCauseTraversal() {
		InternalException root = new InternalException("root", (java.lang.Object) null, null);
		NotAllowedException middle = new NotAllowedException("middle", (java.lang.Object) null, root);
		NotFoundException top = new NotFoundException("top", (java.lang.Object) null, middle);

		assertEquals(top, top.findCause(ErrorKind.NOT_FOUND));
		assertEquals(middle, top.findCause(ErrorKind.NOT_ALLOWED));
		assertEquals(root, top.findCause(ErrorKind.INTERNAL));
		assertNull(top.findCause(ErrorKind.QUERY));
	}

	@Test
	void deepCauseChain() {
		// 4 levels deep
		InternalException e1 = new InternalException("level 1", (java.lang.Object) null, null);
		QueryException e2 = new QueryException("level 2", "\"NotExecuted\"", e1);
		NotAllowedException e3 = new NotAllowedException("level 3",
				"{\"Auth\": \"TokenExpired\"}", e2);
		ValidationException e4 = new ValidationException("level 4", "\"Parse\"", e3);

		assertTrue(e4.hasKind(ErrorKind.INTERNAL));
		assertEquals(e1, e4.findCause(ErrorKind.INTERNAL));
		assertEquals(e2, e4.findCause(ErrorKind.QUERY));
		assertEquals(e3, e4.findCause(ErrorKind.NOT_ALLOWED));
		assertEquals(e4, e4.findCause(ErrorKind.VALIDATION));
	}

	// ---- ServerException properties ----

	@Test
	void messagePreserved() {
		NotAllowedException e = new NotAllowedException("Token expired", (java.lang.Object) null, null);
		assertEquals("Token expired", e.getMessage());
	}

	@Test
	void detailsNull() {
		InternalException e = new InternalException("error", (java.lang.Object) null, null);
		assertNull(e.getDetails());
	}

	@Test
	void detailsFromJsonString() {
		ValidationException e = new ValidationException("parse error", "\"Parse\"", null);
		assertEquals("Parse", e.getDetails());
	}

	@Test
	void detailsFromJsonObject() {
		NotFoundException e = new NotFoundException("not found",
				"{\"Table\": {\"name\": \"users\"}}", null);
		assertTrue(e.getDetails() instanceof Map);
	}

	// ---- SDK-side errors ----

	@Test
	void sdkSideErrorNotServerException() {
		SurrealException e = new SurrealException("sdk error");
		assertFalse(e instanceof ServerException);
	}
}
