package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class JdbcConnectionUrlTests {

	@Test
	void wsWithoutPort() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost");
		assertEquals("ws://localhost", parsed.getNativeConnectString());
		assertNull(parsed.getNamespace());
		assertNull(parsed.getDatabase());
		assertNull(parsed.getUsername());
		assertNull(parsed.getPassword());
	}

	@Test
	void wsWithPort() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000");
		assertEquals("ws://localhost:8000", parsed.getNativeConnectString());
	}

	@Test
	void wssWithUserinfo() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:wss://root:secret@localhost:8000");
		assertEquals("wss://localhost:8000", parsed.getNativeConnectString());
		assertEquals("root", parsed.getUsername());
		assertEquals("secret", parsed.getPassword());
	}

	@Test
	void usernameWithoutPasswordThrows() {
		// A root sign-in always needs a password: passing null through to the
		// native signin() fails with an unhelpful error, so reject the URL up
		// front instead. An explicitly empty password ("user:@host") is still
		// accepted and fails at the server with a genuine auth error.
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb:http://root@localhost:8000"));
	}

	@Test
	void userinfoUsernameCompletedByQueryStringPassword() {
		// Cross-field merging: each of the username/password may come from either
		// the userinfo or the query string, as long as both end up present.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:http://root@localhost:8000?pass=secret");
		assertEquals("http://localhost:8000", parsed.getNativeConnectString());
		assertEquals("root", parsed.getUsername());
		assertEquals("secret", parsed.getPassword());
	}

	@Test
	void explicitlyEmptyPasswordIsAllowed() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://root:@localhost:8000");
		assertEquals("root", parsed.getUsername());
		assertEquals("", parsed.getPassword());
	}

	@Test
	void httpsWithNamespaceAndDatabasePath() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:https://root:root@localhost:8000/test/test");
		assertEquals("https://localhost:8000", parsed.getNativeConnectString());
		assertEquals("root", parsed.getUsername());
		assertEquals("root", parsed.getPassword());
		assertEquals("test", parsed.getNamespace());
		assertEquals("test", parsed.getDatabase());
	}

	@Test
	void wsWithNamespaceOnlyPath() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/test");
		assertEquals("ws://localhost:8000", parsed.getNativeConnectString());
		assertEquals("test", parsed.getNamespace());
		assertNull(parsed.getDatabase());
	}

	@Test
	void queryStringCredentialsInsteadOfUserinfo() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl
				.parse("jdbc:surrealdb://localhost:8000?user=root&pass=root&ns=test&db=test");
		assertEquals("ws://localhost:8000", parsed.getNativeConnectString());
		assertEquals("root", parsed.getUsername());
		assertEquals("root", parsed.getPassword());
		assertEquals("test", parsed.getNamespace());
		assertEquals("test", parsed.getDatabase());
	}

	@Test
	void queryStringLongFormAliases() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl
				.parse("jdbc:surrealdb://localhost:8000?username=root&password=root&namespace=test&database=test");
		assertEquals("root", parsed.getUsername());
		assertEquals("root", parsed.getPassword());
		assertEquals("test", parsed.getNamespace());
		assertEquals("test", parsed.getDatabase());
	}

	@Test
	void userinfoAndPathWinOverQueryString() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl
				.parse("jdbc:surrealdb://root:root@localhost:8000/nsA/dbA?ns=nsB&db=dbB&user=other&pass=other");
		assertEquals("root", parsed.getUsername());
		assertEquals("root", parsed.getPassword());
		assertEquals("nsA", parsed.getNamespace());
		assertEquals("dbA", parsed.getDatabase());
	}

	@Test
	void memory() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:memory");
		assertEquals("memory", parsed.getNativeConnectString());
		assertNull(parsed.getNamespace());
		assertNull(parsed.getDatabase());
	}

	@Test
	void memoryCaseInsensitive() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("JDBC:SurrealDB:MEMORY");
		assertEquals("memory", parsed.getNativeConnectString());
	}

	@Test
	void memoryWithQuerySuffix() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:memory?ns=test&db=test");
		assertEquals("memory", parsed.getNativeConnectString());
		assertEquals("test", parsed.getNamespace());
		assertEquals("test", parsed.getDatabase());
	}

	@Test
	void memSchemeForm() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:mem://anything?ns=test");
		assertEquals("memory", parsed.getNativeConnectString());
		assertEquals("test", parsed.getNamespace());
	}

	@Test
	void embeddedSurrealKvPassthrough() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:surrealkv://some/path");
		assertEquals("surrealkv://some/path", parsed.getNativeConnectString());
		assertNull(parsed.getNamespace());
		assertNull(parsed.getDatabase());
	}

	@Test
	void embeddedSurrealKvPassthroughWithQuerySuffix() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:surrealkv://some/path?ns=test&db=test");
		assertEquals("surrealkv://some/path", parsed.getNativeConnectString());
		assertEquals("test", parsed.getNamespace());
		assertEquals("test", parsed.getDatabase());
	}

	@Test
	void embeddedRocksdbPassthroughUnchanged() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:rocksdb:///var/lib/surreal/data?ns=test");
		assertEquals("rocksdb:///var/lib/surreal/data", parsed.getNativeConnectString());
		assertEquals("test", parsed.getNamespace());
	}

	@Test
	void missingJdbcPrefixThrows() {
		assertThrows(IllegalArgumentException.class, () -> JdbcConnectionUrl.parse("ws://localhost:8000"));
	}

	@Test
	void tooManyPathSegmentsThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/ns/db/extra"));
	}

	@Test
	void wrongSubprotocolThrows() {
		assertThrows(IllegalArgumentException.class, () -> JdbcConnectionUrl.parse("jdbc:mysql://localhost:3306/test"));
	}

	@Test
	void nullInputThrows() {
		assertThrows(IllegalArgumentException.class, () -> JdbcConnectionUrl.parse(null));
	}

	@Test
	void missingHostThrows() {
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://"));
		// The error message should echo back the URL the caller actually passed, not
		// an internally-rewritten form (e.g. with a default scheme spliced in).
		assertTrue(ex.getMessage().contains("jdbc:surrealdb://"));
	}

	@Test
	void missingHostWithExplicitSchemeThrows() {
		assertThrows(IllegalArgumentException.class, () -> JdbcConnectionUrl.parse("jdbc:surrealdb:wss://"));
	}

	@Test
	void emptyNamespaceSegmentThrows() {
		// "//db" (empty namespace, non-empty database) must not silently be treated
		// as namespace="" - that would later cause a confusing useNs("") call.
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000//db"));
	}

	@Test
	void emptyDatabaseSegmentThrows() {
		// Two segments where the second (database) is empty - not to be confused
		// with a single trailing slash ("/ns/"), which legitimately means "namespace
		// only, no database" (see trailingSlashAfterNamespaceOnlyIsAllowed).
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/ns//"));
	}

	@Test
	void trailingSlashAfterNamespaceOnlyIsAllowed() {
		// A single trailing slash with no database segment is fine (not the same as
		// an empty database segment): "ns/" means "namespace=ns, no database".
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/ns/");
		assertEquals("ns", parsed.getNamespace());
		assertNull(parsed.getDatabase());
	}

	@Test
	void trailingSlashAfterNamespaceAndDatabaseIsAllowed() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/ns/db/");
		assertEquals("ns", parsed.getNamespace());
		assertEquals("db", parsed.getDatabase());
	}

	@Test
	void databaseWithoutNamespaceThrows() {
		// "?db=..." with no namespace from either the path or "?ns=..." used to be
		// silently dropped by connect() (a database cannot be selected without a
		// namespace); the URL is now rejected outright instead.
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000?db=foo"));
	}

	@Test
	void databaseWithoutNamespaceThrowsForMemory() {
		assertThrows(IllegalArgumentException.class, () -> JdbcConnectionUrl.parse("jdbc:surrealdb:memory?db=foo"));
	}

	@Test
	void pathNamespaceWithQueryStringDatabase() {
		// The namespace and database may come from different sources, as long as a
		// database never ends up without a namespace.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/ns?db=foo");
		assertEquals("ns", parsed.getNamespace());
		assertEquals("foo", parsed.getDatabase());
	}

	@Test
	void emptyUserinfoYieldsNoCredentials() {
		// "@host" supplies no credentials; normalize to null (no sign-in attempt)
		// rather than attempting a root sign-in as user "" with no password.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://@localhost:8000");
		assertEquals("ws://localhost:8000", parsed.getNativeConnectString());
		assertNull(parsed.getUsername());
		assertNull(parsed.getPassword());
	}

	@Test
	void emptyUsernameAndPasswordYieldsNoCredentials() {
		// ":@host" is treated the same as "@host": no credentials.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://:@localhost:8000");
		assertNull(parsed.getUsername());
		assertNull(parsed.getPassword());
	}

	@Test
	void emptyUserinfoDoesNotShadowQueryStringCredentials() {
		// An empty userinfo counts as absent, so it must not out-prioritize
		// explicit query-string credentials.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://@localhost:8000?user=root&pass=root");
		assertEquals("root", parsed.getUsername());
		assertEquals("root", parsed.getPassword());
	}

	@Test
	void passwordWithoutUsernameThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://:secret@localhost:8000"));
	}

	@Test
	void queryStringPasswordWithoutUsernameThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000?pass=secret"));
	}

	@Test
	void ipv6HostLiteral() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://[::1]:8000/ns/db");
		assertEquals("ws://[::1]:8000", parsed.getNativeConnectString());
		assertEquals("ns", parsed.getNamespace());
		assertEquals("db", parsed.getDatabase());
	}

	@Test
	void percentEncodedPasswordWithSpecialCharacters() {
		// Password "p@ss/w:ord" percent-encoded so the reserved delimiter characters
		// ('@', '/', ':') don't get misinterpreted as URL structure.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://user:p%40ss%2Fw%3Aord@localhost:8000");
		assertEquals("user", parsed.getUsername());
		assertEquals("p@ss/w:ord", parsed.getPassword());
	}

	@Test
	void plusInUserinfoIsPreservedLiterally() {
		// '+' is an ordinary literal character in URI userinfo (RFC 3986); only
		// form-encoded query values treat it as an encoded space. "pa+ss" must
		// therefore parse as the password "pa+ss", not "pa ss" - a silent mangling
		// that would only surface later as an authentication failure.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://ro+ot:pa+ss@localhost:8000");
		assertEquals("ro+ot", parsed.getUsername());
		assertEquals("pa+ss", parsed.getPassword());
	}

	@Test
	void plusInPathSegmentsIsPreservedLiterally() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/my+ns/my+db");
		assertEquals("my+ns", parsed.getNamespace());
		assertEquals("my+db", parsed.getDatabase());
	}

	@Test
	void plusInQueryValuesStillDecodesToSpace() {
		// Query-string values keep application/x-www-form-urlencoded semantics,
		// where '+' genuinely means space; a literal '+' there is written %2B.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000?user=ro+ot&pass=pa+ss");
		assertEquals("ro ot", parsed.getUsername());
		assertEquals("pa ss", parsed.getPassword());
	}

	@Test
	void percentEncodedPlusInUserinfo() {
		// %2B must keep decoding to a literal '+' in userinfo, same as a bare '+'.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://user:pa%2Bss@localhost:8000");
		assertEquals("pa+ss", parsed.getPassword());
	}

	@Test
	void percentEncodedMultiByteUtf8InUserinfoAndPath() {
		// Multi-byte percent-encoded UTF-8 sequences must still expand in userinfo
		// and path components.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://user:p%C3%A4ss@localhost:8000/n%C3%A4s");
		assertEquals("päss", parsed.getPassword());
		assertEquals("näs", parsed.getNamespace());
	}

	@Test
	void uppercaseSchemeIsNormalizedToLowercaseForNativeConnect() {
		// The underlying native "any" engine dispatch matches scheme tokens
		// case-sensitively (lowercase only), so an upper/mixed-case scheme in the
		// JDBC URL must be normalized before being handed to the native connect()
		// call, or the connection will be rejected outright even though this
		// parser accepts the scheme token case-insensitively.
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:WSS://root:root@localhost:8000");
		assertEquals("wss://localhost:8000", parsed.getNativeConnectString());
	}

	@Test
	void uppercaseDefaultSchemeIsNormalizedToLowercase() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("JDBC:SURREALDB://LOCALHOST:8000");
		// Host casing is left untouched (hostnames may legitimately be mixed-case),
		// only the synthesized scheme token must be lowercase.
		assertEquals("ws://LOCALHOST:8000", parsed.getNativeConnectString());
	}
}
