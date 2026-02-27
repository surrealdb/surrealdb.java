package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the structured error handling classes.
 * These tests construct exceptions directly (no JNI) to verify the Java-side
 * class hierarchy, detail parsing, convenience getters, and cause chain traversal.
 *
 * <p>Each accessor is tested with both the new internally-tagged format
 * ({@code {"kind": "...", "details": ...}}) and the legacy externally-tagged
 * format ({@code "..." / {"...": ...}}).
 */
public class ErrorTests {

	/** New format: unit variant {@code {"kind": "X"}}. */
	private static Map<String, java.lang.Object> nf(String kind) {
		Map<String, java.lang.Object> m = new LinkedHashMap<>();
		m.put("kind", kind);
		return m;
	}

	/** New format: struct variant {@code {"kind": "X", "details": {...}}}. */
	private static Map<String, java.lang.Object> nf(String kind, Map<String, java.lang.Object> details) {
		Map<String, java.lang.Object> m = new LinkedHashMap<>();
		m.put("kind", kind);
		m.put("details", details);
		return m;
	}

	/** New format: newtype variant {@code {"kind": "X", "details": {"kind": "Y"}}}. */
	private static Map<String, java.lang.Object> nfNested(String kind, String innerKind) {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("kind", innerKind);
		return nf(kind, inner);
	}

	/** Old format: single key -> value (object). */
	private static Map<String, java.lang.Object> of(String key, Map<String, java.lang.Object> value) {
		Map<String, java.lang.Object> m = new LinkedHashMap<>();
		m.put(key, value);
		return m;
	}

	/** Old format: single key -> string (newtype). */
	private static Map<String, java.lang.Object> ofStr(String key, String value) {
		Map<String, java.lang.Object> m = new LinkedHashMap<>();
		m.put(key, value);
		return m;
	}

	// ---- ErrorKind enum: wire strings via getRaw() ----

	@Test
	void errorKindConstants() {
		assertEquals("Validation", ErrorKind.VALIDATION.getRaw());
		assertEquals("Configuration", ErrorKind.CONFIGURATION.getRaw());
		assertEquals("Thrown", ErrorKind.THROWN.getRaw());
		assertEquals("Query", ErrorKind.QUERY.getRaw());
		assertEquals("Serialization", ErrorKind.SERIALIZATION.getRaw());
		assertEquals("NotAllowed", ErrorKind.NOT_ALLOWED.getRaw());
		assertEquals("NotFound", ErrorKind.NOT_FOUND.getRaw());
		assertEquals("AlreadyExists", ErrorKind.ALREADY_EXISTS.getRaw());
		assertEquals("Connection", ErrorKind.CONNECTION.getRaw());
		assertEquals("Internal", ErrorKind.INTERNAL.getRaw());
	}

	// ---- Detail kind constants ----

	@Test
	void authDetailKindConstants() {
		assertEquals("TokenExpired", AuthDetailKind.TOKEN_EXPIRED);
		assertEquals("SessionExpired", AuthDetailKind.SESSION_EXPIRED);
		assertEquals("InvalidAuth", AuthDetailKind.INVALID_AUTH);
		assertEquals("UnexpectedAuth", AuthDetailKind.UNEXPECTED_AUTH);
		assertEquals("MissingUserOrPass", AuthDetailKind.MISSING_USER_OR_PASS);
		assertEquals("NoSigninTarget", AuthDetailKind.NO_SIGNIN_TARGET);
		assertEquals("InvalidPass", AuthDetailKind.INVALID_PASS);
		assertEquals("TokenMakingFailed", AuthDetailKind.TOKEN_MAKING_FAILED);
		assertEquals("InvalidSignup", AuthDetailKind.INVALID_SIGNUP);
		assertEquals("InvalidRole", AuthDetailKind.INVALID_ROLE);
		assertEquals("NotAllowed", AuthDetailKind.NOT_ALLOWED);
	}

	@Test
	void validationDetailKindConstants() {
		assertEquals("Parse", ValidationDetailKind.PARSE);
		assertEquals("InvalidRequest", ValidationDetailKind.INVALID_REQUEST);
		assertEquals("InvalidParams", ValidationDetailKind.INVALID_PARAMS);
		assertEquals("NamespaceEmpty", ValidationDetailKind.NAMESPACE_EMPTY);
		assertEquals("DatabaseEmpty", ValidationDetailKind.DATABASE_EMPTY);
		assertEquals("InvalidParameter", ValidationDetailKind.INVALID_PARAMETER);
		assertEquals("InvalidContent", ValidationDetailKind.INVALID_CONTENT);
		assertEquals("InvalidMerge", ValidationDetailKind.INVALID_MERGE);
	}

	@Test
	void configurationDetailKindConstants() {
		assertEquals("LiveQueryNotSupported", ConfigurationDetailKind.LIVE_QUERY_NOT_SUPPORTED);
		assertEquals("BadLiveQueryConfig", ConfigurationDetailKind.BAD_LIVE_QUERY_CONFIG);
		assertEquals("BadGraphqlConfig", ConfigurationDetailKind.BAD_GRAPHQL_CONFIG);
	}

	@Test
	void queryDetailKindConstants() {
		assertEquals("NotExecuted", QueryDetailKind.NOT_EXECUTED);
		assertEquals("TimedOut", QueryDetailKind.TIMED_OUT);
		assertEquals("Cancelled", QueryDetailKind.CANCELLED);
	}

	@Test
	void serializationDetailKindConstants() {
		assertEquals("Serialization", SerializationDetailKind.SERIALIZATION);
		assertEquals("Deserialization", SerializationDetailKind.DESERIALIZATION);
	}

	@Test
	void notAllowedDetailKindConstants() {
		assertEquals("Scripting", NotAllowedDetailKind.SCRIPTING);
		assertEquals("Auth", NotAllowedDetailKind.AUTH);
		assertEquals("Method", NotAllowedDetailKind.METHOD);
		assertEquals("Function", NotAllowedDetailKind.FUNCTION);
		assertEquals("Target", NotAllowedDetailKind.TARGET);
	}

	@Test
	void notFoundDetailKindConstants() {
		assertEquals("Method", NotFoundDetailKind.METHOD);
		assertEquals("Session", NotFoundDetailKind.SESSION);
		assertEquals("Table", NotFoundDetailKind.TABLE);
		assertEquals("Record", NotFoundDetailKind.RECORD);
		assertEquals("Namespace", NotFoundDetailKind.NAMESPACE);
		assertEquals("Database", NotFoundDetailKind.DATABASE);
		assertEquals("Transaction", NotFoundDetailKind.TRANSACTION);
	}

	@Test
	void alreadyExistsDetailKindConstants() {
		assertEquals("Session", AlreadyExistsDetailKind.SESSION);
		assertEquals("Table", AlreadyExistsDetailKind.TABLE);
		assertEquals("Record", AlreadyExistsDetailKind.RECORD);
		assertEquals("Namespace", AlreadyExistsDetailKind.NAMESPACE);
		assertEquals("Database", AlreadyExistsDetailKind.DATABASE);
	}

	@Test
	void connectionDetailKindConstants() {
		assertEquals("Uninitialised", ConnectionDetailKind.UNINITIALISED);
		assertEquals("AlreadyConnected", ConnectionDetailKind.ALREADY_CONNECTED);
	}

	// ---- Exception hierarchy ----

	@Test
	void hierarchyValidation() {
		ValidationException e = new ValidationException("parse error", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertTrue(e instanceof SurrealException);
		assertTrue(e instanceof RuntimeException);
		assertEquals(ErrorKind.VALIDATION, e.getKindEnum());
	}

	@Test
	void hierarchyConfiguration() {
		ConfigurationException e = new ConfigurationException("not supported", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.CONFIGURATION, e.getKindEnum());
	}

	@Test
	void hierarchyThrown() {
		ThrownException e = new ThrownException("user error", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.THROWN, e.getKindEnum());
	}

	@Test
	void hierarchyQuery() {
		QueryException e = new QueryException("timeout", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.QUERY, e.getKindEnum());
	}

	@Test
	void hierarchySerialization() {
		SerializationException e = new SerializationException("bad data", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.SERIALIZATION, e.getKindEnum());
	}

	@Test
	void hierarchyNotAllowed() {
		NotAllowedException e = new NotAllowedException("denied", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.NOT_ALLOWED, e.getKindEnum());
	}

	@Test
	void hierarchyNotFound() {
		NotFoundException e = new NotFoundException("missing", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.NOT_FOUND, e.getKindEnum());
	}

	@Test
	void hierarchyAlreadyExists() {
		AlreadyExistsException e = new AlreadyExistsException("duplicate", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.ALREADY_EXISTS, e.getKindEnum());
	}

	@Test
	void hierarchyInternal() {
		InternalException e = new InternalException("unexpected", (java.lang.Object) null, null);
		assertTrue(e instanceof ServerException);
		assertEquals(ErrorKind.INTERNAL, e.getKindEnum());
	}

	@Test
	void hierarchyServerException() {
		ServerException e = new ServerException(ErrorKind.CONNECTION, null, "conn error", (java.lang.Object) null, null);
		assertTrue(e instanceof SurrealException);
		assertFalse(e instanceof InternalException);
		assertEquals(ErrorKind.CONNECTION, e.getKindEnum());
	}

	@Test
	void unknownKindProducesBaseServerException() {
		ServerException e = new ServerException("FutureKind", "future error", (java.lang.Object) null, null);
		assertEquals("FutureKind", e.getKind());
		assertFalse(e instanceof InternalException);
	}

	@Test
	void catchAllWithSurrealException() {
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

	// Detail structure is built on the native (Rust) side and passed as Object (Map/List/String/Number).
	// The tests below verify the Java detail helpers work with that structure.

	// ---- detailKind / detailInner ----

	@Test
	void detailKindFromNewFormat() {
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Parse");
		assertEquals("Parse", ServerException.detailKind(details));
	}

	@Test
	void detailKindFromOldFormatReturnsNull() {
		assertNull(ServerException.detailKind("Parse"));
		assertNull(ServerException.detailKind(null));
		Map<String, java.lang.Object> old = new HashMap<>();
		old.put("Auth", "TokenExpired");
		assertNull(ServerException.detailKind(old));
	}

	@Test
	void detailInnerFromNewFormat() {
		Map<String, java.lang.Object> innerDetails = new HashMap<>();
		innerDetails.put("kind", "TokenExpired");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Auth");
		details.put("details", innerDetails);
		assertEquals(innerDetails, ServerException.detailInner(details));
	}

	@Test
	void detailInnerReturnsNullWhenMissing() {
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Parse");
		assertNull(ServerException.detailInner(details));
		assertNull(ServerException.detailInner(null));
		assertNull(ServerException.detailInner("Parse"));
	}

	// ---- hasDetailKey (dual format) ----

	@Test
	void hasDetailKeyNewFormatUnit() {
		// {"kind": "Parse"}
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Parse");
		assertTrue(ServerException.hasDetailKey(details, "Parse"));
		assertFalse(ServerException.hasDetailKey(details, "Other"));
	}

	@Test
	void hasDetailKeyOldFormatString() {
		assertTrue(ServerException.hasDetailKey("Parse", "Parse"));
		assertFalse(ServerException.hasDetailKey("Parse", "Other"));
	}

	@Test
	void hasDetailKeyOldFormatMap() {
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", new HashMap<>());
		assertTrue(ServerException.hasDetailKey(details, "Table"));
		assertFalse(ServerException.hasDetailKey(details, "Record"));
	}

	@Test
	void hasDetailKeyNull() {
		assertFalse(ServerException.hasDetailKey(null, "Parse"));
	}

	// ---- getDetailValue (dual format) ----

	@Test
	void getDetailValueNewFormat() {
		// {"kind": "Auth", "details": {"kind": "TokenExpired"}}
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("kind", "TokenExpired");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Auth");
		details.put("details", inner);
		assertEquals(inner, ServerException.getDetailValue(details, "Auth"));
		assertNull(ServerException.getDetailValue(details, "Other"));
	}

	@Test
	void getDetailValueNewFormatUnitReturnsNull() {
		// {"kind": "Parse"} -- unit variant, no inner details
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Parse");
		assertNull(ServerException.getDetailValue(details, "Parse"));
	}

	@Test
	void getDetailValueOldFormatString() {
		assertNull(ServerException.getDetailValue("Parse", "Parse"));
	}

	@Test
	void getDetailValueOldFormatMap() {
		Map<String, java.lang.Object> inner = new HashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", inner);
		assertEquals(inner, ServerException.getDetailValue(details, "Table"));
		assertNull(ServerException.getDetailValue(details, "Other"));
	}

	// ---- detailField (dual format) ----

	@Test
	void detailFieldNewFormat() {
		// {"kind": "Table", "details": {"name": "users"}}
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Table");
		details.put("details", inner);
		assertEquals("users", ServerException.detailField(details, "Table", "name"));
		assertNull(ServerException.detailField(details, "Table", "missing"));
		assertNull(ServerException.detailField(details, "Other", "name"));
	}

	@Test
	void detailFieldOldFormat() {
		Map<String, java.lang.Object> inner = new HashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", inner);
		assertEquals("users", ServerException.detailField(details, "Table", "name"));
		assertNull(ServerException.detailField(details, "Table", "missing"));
		assertNull(ServerException.detailField(details, "Other", "name"));
	}

	// ---- getDetailString (dual format) ----

	@Test
	void getDetailStringNewFormat() {
		// {"kind": "Auth", "details": {"kind": "TokenExpired"}}
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("kind", "TokenExpired");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Auth");
		details.put("details", inner);
		assertEquals("TokenExpired", ServerException.getDetailString(details, "Auth"));
	}

	@Test
	void getDetailStringOldFormat() {
		// {"Auth": "TokenExpired"}
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Auth", "TokenExpired");
		assertEquals("TokenExpired", ServerException.getDetailString(details, "Auth"));
	}

	@Test
	void getDetailStringReturnsNullForMismatch() {
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Auth", "TokenExpired");
		assertNull(ServerException.getDetailString(details, "Other"));
		assertNull(ServerException.getDetailString(null, "Auth"));
	}

	// ---- Legacy helpers still work ----

	@Test
	void getNestedStringDelegatesToDetailField() {
		Map<String, java.lang.Object> inner = new HashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", inner);
		assertEquals("users", ServerException.getNestedString(details, "Table", "name"));
	}

	@Test
	void isNewtypeValueDelegatesToGetDetailString() {
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Auth", "TokenExpired");
		assertTrue(ServerException.isNewtypeValue(details, "Auth", "TokenExpired"));
		assertFalse(ServerException.isNewtypeValue(details, "Auth", "InvalidAuth"));
	}

	// ========================================================================
	// Convenience getters on subclasses -- NEW FORMAT
	// ========================================================================

	// ---- ValidationException (new format) ----

	@Test
	void validationParseErrorNewFormat() {
		ValidationException e = new ValidationException("parse error", nf("Parse"), null);
		assertTrue(e.isParseError());
		assertNull(e.getParameterName());
	}

	@Test
	void validationInvalidParameterNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "foo");
		ValidationException e = new ValidationException("bad param", nf("InvalidParameter", inner), null);
		assertFalse(e.isParseError());
		assertEquals("foo", e.getParameterName());
	}

	// ---- ConfigurationException (new format) ----

	@Test
	void configurationLiveQueryNotSupportedNewFormat() {
		ConfigurationException e = new ConfigurationException("not supported", nf("LiveQueryNotSupported"), null);
		assertTrue(e.isLiveQueryNotSupported());
	}

	// ---- QueryException (new format) ----

	@Test
	void queryNotExecutedNewFormat() {
		QueryException e = new QueryException("not executed", nf("NotExecuted"), null);
		assertTrue(e.isNotExecuted());
		assertFalse(e.isTimedOut());
		assertFalse(e.isCancelled());
		assertNull(e.getTimeout());
	}

	@Test
	void queryTimedOutNewFormat() {
		Map<String, java.lang.Object> duration = new LinkedHashMap<>();
		duration.put("secs", 30);
		duration.put("nanos", 500);
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("duration", duration);
		QueryException e = new QueryException("timed out", nf("TimedOut", inner), null);
		assertTrue(e.isTimedOut());
		assertFalse(e.isNotExecuted());
		Map<String, java.lang.Object> timeout = e.getTimeout();
		assertNotNull(timeout);
		assertEquals(30, timeout.get("secs"));
		assertEquals(500, timeout.get("nanos"));
	}

	@Test
	void queryCancelledNewFormat() {
		QueryException e = new QueryException("cancelled", nf("Cancelled"), null);
		assertTrue(e.isCancelled());
		assertFalse(e.isTimedOut());
	}

	// ---- SerializationException (new format) ----

	@Test
	void serializationDeserializationNewFormat() {
		SerializationException e = new SerializationException("deser error", nf("Deserialization"), null);
		assertTrue(e.isDeserialization());
	}

	@Test
	void serializationSerializationNewFormat() {
		SerializationException e = new SerializationException("ser error", nf("Serialization"), null);
		assertFalse(e.isDeserialization());
	}

	// ---- NotAllowedException (new format) ----

	@Test
	void notAllowedTokenExpiredNewFormat() {
		NotAllowedException e = new NotAllowedException("token expired", nfNested("Auth", "TokenExpired"), null);
		assertTrue(e.isTokenExpired());
		assertFalse(e.isInvalidAuth());
		assertFalse(e.isScriptingBlocked());
		assertNull(e.getMethodName());
		assertNull(e.getFunctionName());
		assertNull(e.getTargetName());
	}

	@Test
	void notAllowedInvalidAuthNewFormat() {
		NotAllowedException e = new NotAllowedException("invalid auth", nfNested("Auth", "InvalidAuth"), null);
		assertTrue(e.isInvalidAuth());
		assertFalse(e.isTokenExpired());
	}

	@Test
	void notAllowedScriptingNewFormat() {
		NotAllowedException e = new NotAllowedException("scripting blocked", nf("Scripting"), null);
		assertTrue(e.isScriptingBlocked());
		assertFalse(e.isTokenExpired());
	}

	@Test
	void notAllowedMethodNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "query");
		NotAllowedException e = new NotAllowedException("method blocked", nf("Method", inner), null);
		assertEquals("query", e.getMethodName());
		assertNull(e.getFunctionName());
		assertNull(e.getTargetName());
	}

	@Test
	void notAllowedFunctionNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "http::get");
		NotAllowedException e = new NotAllowedException("fn blocked", nf("Function", inner), null);
		assertEquals("http::get", e.getFunctionName());
		assertNull(e.getMethodName());
	}

	@Test
	void notAllowedTargetNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "some_target");
		NotAllowedException e = new NotAllowedException("target blocked", nf("Target", inner), null);
		assertEquals("some_target", e.getTargetName());
		assertNull(e.getMethodName());
		assertNull(e.getFunctionName());
	}

	// ---- NotFoundException (new format) ----

	@Test
	void notFoundTableNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "users");
		NotFoundException e = new NotFoundException("table not found", nf("Table", inner), null);
		assertEquals("users", e.getTableName());
		assertNull(e.getRecordId());
		assertNull(e.getMethodName());
		assertNull(e.getNamespaceName());
		assertNull(e.getDatabaseName());
		assertNull(e.getSessionId());
	}

	@Test
	void notFoundRecordNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("id", "person:1");
		NotFoundException e = new NotFoundException("record not found", nf("Record", inner), null);
		assertEquals("person:1", e.getRecordId());
		assertNull(e.getTableName());
	}

	@Test
	void notFoundMethodNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "unknown");
		NotFoundException e = new NotFoundException("method not found", nf("Method", inner), null);
		assertEquals("unknown", e.getMethodName());
	}

	@Test
	void notFoundNamespaceNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "test_ns");
		NotFoundException e = new NotFoundException("ns not found", nf("Namespace", inner), null);
		assertEquals("test_ns", e.getNamespaceName());
	}

	@Test
	void notFoundDatabaseNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "test_db");
		NotFoundException e = new NotFoundException("db not found", nf("Database", inner), null);
		assertEquals("test_db", e.getDatabaseName());
	}

	@Test
	void notFoundSessionNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("id", "sess_123");
		NotFoundException e = new NotFoundException("session not found", nf("Session", inner), null);
		assertEquals("sess_123", e.getSessionId());
	}

	// ---- AlreadyExistsException (new format) ----

	@Test
	void alreadyExistsRecordNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("id", "person:1");
		AlreadyExistsException e = new AlreadyExistsException("duplicate record", nf("Record", inner), null);
		assertEquals("person:1", e.getRecordId());
		assertNull(e.getTableName());
		assertNull(e.getSessionId());
		assertNull(e.getNamespaceName());
		assertNull(e.getDatabaseName());
	}

	@Test
	void alreadyExistsTableNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "users");
		AlreadyExistsException e = new AlreadyExistsException("duplicate table", nf("Table", inner), null);
		assertEquals("users", e.getTableName());
		assertNull(e.getRecordId());
	}

	@Test
	void alreadyExistsSessionNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("id", "sess_abc");
		AlreadyExistsException e = new AlreadyExistsException("duplicate session", nf("Session", inner), null);
		assertEquals("sess_abc", e.getSessionId());
	}

	@Test
	void alreadyExistsNamespaceNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "ns1");
		AlreadyExistsException e = new AlreadyExistsException("duplicate namespace", nf("Namespace", inner), null);
		assertEquals("ns1", e.getNamespaceName());
	}

	@Test
	void alreadyExistsDatabaseNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "db1");
		AlreadyExistsException e = new AlreadyExistsException("duplicate database", nf("Database", inner), null);
		assertEquals("db1", e.getDatabaseName());
	}

	// ========================================================================
	// Convenience getters on subclasses -- OLD (legacy) FORMAT
	// ========================================================================

	@Test
	void validationParseErrorOldFormat() {
		ValidationException e = new ValidationException("parse error", "Parse", null);
		assertTrue(e.isParseError());
		assertNull(e.getParameterName());
	}

	@Test
	void validationInvalidParameterOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "foo");
		ValidationException e = new ValidationException("bad param", of("InvalidParameter", inner), null);
		assertFalse(e.isParseError());
		assertEquals("foo", e.getParameterName());
	}

	@Test
	void configurationLiveQueryNotSupportedOldFormat() {
		ConfigurationException e = new ConfigurationException("not supported", "LiveQueryNotSupported", null);
		assertTrue(e.isLiveQueryNotSupported());
	}

	@Test
	void queryNotExecutedOldFormat() {
		QueryException e = new QueryException("not executed", "NotExecuted", null);
		assertTrue(e.isNotExecuted());
		assertFalse(e.isTimedOut());
		assertFalse(e.isCancelled());
		assertNull(e.getTimeout());
	}

	@Test
	void queryTimedOutOldFormat() {
		Map<String, java.lang.Object> duration = new LinkedHashMap<>();
		duration.put("secs", 30);
		duration.put("nanos", 500);
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("duration", duration);
		QueryException e = new QueryException("timed out", of("TimedOut", inner), null);
		assertTrue(e.isTimedOut());
		assertFalse(e.isNotExecuted());
		Map<String, java.lang.Object> timeout = e.getTimeout();
		assertNotNull(timeout);
		assertEquals(30, timeout.get("secs"));
		assertEquals(500, timeout.get("nanos"));
	}

	@Test
	void queryCancelledOldFormat() {
		QueryException e = new QueryException("cancelled", "Cancelled", null);
		assertTrue(e.isCancelled());
		assertFalse(e.isTimedOut());
	}

	@Test
	void serializationDeserializationOldFormat() {
		SerializationException e = new SerializationException("deser error", "Deserialization", null);
		assertTrue(e.isDeserialization());
	}

	@Test
	void serializationSerializationOldFormat() {
		SerializationException e = new SerializationException("ser error", "Serialization", null);
		assertFalse(e.isDeserialization());
	}

	@Test
	void notAllowedTokenExpiredOldFormat() {
		NotAllowedException e = new NotAllowedException("token expired", ofStr("Auth", "TokenExpired"), null);
		assertTrue(e.isTokenExpired());
		assertFalse(e.isInvalidAuth());
		assertFalse(e.isScriptingBlocked());
		assertNull(e.getMethodName());
		assertNull(e.getFunctionName());
	}

	@Test
	void notAllowedInvalidAuthOldFormat() {
		NotAllowedException e = new NotAllowedException("invalid auth", ofStr("Auth", "InvalidAuth"), null);
		assertTrue(e.isInvalidAuth());
		assertFalse(e.isTokenExpired());
	}

	@Test
	void notAllowedScriptingOldFormat() {
		NotAllowedException e = new NotAllowedException("scripting blocked", "Scripting", null);
		assertTrue(e.isScriptingBlocked());
		assertFalse(e.isTokenExpired());
	}

	@Test
	void notAllowedMethodOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "query");
		NotAllowedException e = new NotAllowedException("method blocked", of("Method", inner), null);
		assertEquals("query", e.getMethodName());
		assertNull(e.getFunctionName());
	}

	@Test
	void notAllowedFunctionOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "http::get");
		NotAllowedException e = new NotAllowedException("fn blocked", of("Function", inner), null);
		assertEquals("http::get", e.getFunctionName());
		assertNull(e.getMethodName());
	}

	@Test
	void notFoundTableOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "users");
		NotFoundException e = new NotFoundException("table not found", of("Table", inner), null);
		assertEquals("users", e.getTableName());
		assertNull(e.getRecordId());
		assertNull(e.getMethodName());
		assertNull(e.getNamespaceName());
		assertNull(e.getDatabaseName());
	}

	@Test
	void notFoundRecordOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("id", "person:1");
		NotFoundException e = new NotFoundException("record not found", of("Record", inner), null);
		assertEquals("person:1", e.getRecordId());
		assertNull(e.getTableName());
	}

	@Test
	void notFoundMethodOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "unknown");
		NotFoundException e = new NotFoundException("method not found", of("Method", inner), null);
		assertEquals("unknown", e.getMethodName());
	}

	@Test
	void notFoundNamespaceOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "test_ns");
		NotFoundException e = new NotFoundException("ns not found", of("Namespace", inner), null);
		assertEquals("test_ns", e.getNamespaceName());
	}

	@Test
	void notFoundDatabaseOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "test_db");
		NotFoundException e = new NotFoundException("db not found", of("Database", inner), null);
		assertEquals("test_db", e.getDatabaseName());
	}

	@Test
	void alreadyExistsRecordOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("id", "person:1");
		AlreadyExistsException e = new AlreadyExistsException("duplicate record", of("Record", inner), null);
		assertEquals("person:1", e.getRecordId());
		assertNull(e.getTableName());
	}

	@Test
	void alreadyExistsTableOldFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "users");
		AlreadyExistsException e = new AlreadyExistsException("duplicate table", of("Table", inner), null);
		assertEquals("users", e.getTableName());
		assertNull(e.getRecordId());
	}

	// ========================================================================
	// Cause chain
	// ========================================================================

	@Test
	void causeChainStandard() {
		InternalException root = new InternalException("root", (java.lang.Object) null, null);
		NotAllowedException middle = new NotAllowedException("middle", nfNested("Auth", "TokenExpired"), root);
		Map<String, java.lang.Object> tableDetails = new LinkedHashMap<>();
		tableDetails.put("name", "users");
		NotFoundException top = new NotFoundException("top", nf("Table", tableDetails), middle);

		assertEquals(middle, top.getCause());
		assertEquals(root, middle.getCause());
		assertNull(root.getCause());

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
		InternalException e1 = new InternalException("level 1", (java.lang.Object) null, null);
		QueryException e2 = new QueryException("level 2", nf("NotExecuted"), e1);
		NotAllowedException e3 = new NotAllowedException("level 3", nfNested("Auth", "TokenExpired"), e2);
		ValidationException e4 = new ValidationException("level 4", nf("Parse"), e3);

		assertTrue(e4.hasKind(ErrorKind.INTERNAL));
		assertEquals(e1, e4.findCause(ErrorKind.INTERNAL));
		assertEquals(e2, e4.findCause(ErrorKind.QUERY));
		assertEquals(e3, e4.findCause(ErrorKind.NOT_ALLOWED));
		assertEquals(e4, e4.findCause(ErrorKind.VALIDATION));
	}

	// ========================================================================
	// ServerException properties
	// ========================================================================

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
	void detailsFromString() {
		ValidationException e = new ValidationException("parse error", "Parse", null);
		assertEquals("Parse", e.getDetails());
	}

	@Test
	void detailsFromMap() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Table");
		details.put("details", inner);
		NotFoundException e = new NotFoundException("not found", details, null);
		assertTrue(e.getDetails() instanceof Map);
	}

	// ---- SDK-side errors ----

	@Test
	void sdkSideErrorNotServerException() {
		SurrealException e = new SurrealException("sdk error");
		assertFalse(e instanceof ServerException);
	}

	// ========================================================================
	// Double-encoding compatibility (3.0.0 -> 3.0.1)
	// ========================================================================
	// The double-encoding unwrap happens in the Rust JNI bridge, not in Java.
	// These tests verify the Java helpers handle the inner structure correctly
	// after unwrapping, as well as the edge case where details accidentally
	// contain a "kind" key matching the error kind (simulating pre-unwrap state).

	@Test
	void doubleWrappedDetailsManuallyUnwrapped() {
		// After the Rust bridge unwraps, Java receives:
		//   {"kind": "Record", "details": {"id": "person:dup"}}
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("id", "person:dup");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Record");
		details.put("details", inner);
		AlreadyExistsException e = new AlreadyExistsException("duplicate", details, null);
		assertEquals("person:dup", e.getRecordId());
	}

	@Test
	void deeplyNestedAuthDetails() {
		// Full depth: NotAllowed -> Auth -> TokenExpired (new format)
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("kind", "TokenExpired");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Auth");
		details.put("details", inner);
		NotAllowedException e = new NotAllowedException("auth failed", details, null);
		assertTrue(e.isTokenExpired());
		assertFalse(e.isInvalidAuth());

		// Full depth: NotAllowed -> Auth -> InvalidAuth (new format)
		Map<String, java.lang.Object> inner2 = new LinkedHashMap<>();
		inner2.put("kind", "InvalidAuth");
		Map<String, java.lang.Object> details2 = new LinkedHashMap<>();
		details2.put("kind", "Auth");
		details2.put("details", inner2);
		NotAllowedException e2 = new NotAllowedException("auth failed", details2, null);
		assertTrue(e2.isInvalidAuth());
		assertFalse(e2.isTokenExpired());
	}

	@Test
	void deeplyNestedAuthWithRole() {
		// NotAllowed -> Auth -> InvalidRole with details
		Map<String, java.lang.Object> roleDetails = new LinkedHashMap<>();
		roleDetails.put("name", "admin");
		Map<String, java.lang.Object> invalidRoleInner = new LinkedHashMap<>();
		invalidRoleInner.put("kind", "InvalidRole");
		invalidRoleInner.put("details", roleDetails);
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Auth");
		details.put("details", invalidRoleInner);
		NotAllowedException e = new NotAllowedException("bad role", details, null);
		assertFalse(e.isTokenExpired());
		assertFalse(e.isInvalidAuth());
	}

	// ========================================================================
	// Object-constructed details (no JSON, direct Map)
	// ========================================================================

	@Test
	void objectConstructedNewFormat() {
		Map<String, java.lang.Object> inner = new LinkedHashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new LinkedHashMap<>();
		details.put("kind", "Table");
		details.put("details", inner);
		NotFoundException e = new NotFoundException("not found", details, null);
		assertEquals("users", e.getTableName());
	}

	@Test
	void objectConstructedOldFormat() {
		Map<String, java.lang.Object> inner = new HashMap<>();
		inner.put("name", "users");
		Map<String, java.lang.Object> details = new HashMap<>();
		details.put("Table", inner);
		NotFoundException e = new NotFoundException("not found", details, null);
		assertEquals("users", e.getTableName());
	}
}
