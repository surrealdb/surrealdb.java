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
	void httpWithUserinfoNoPassword() {
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb:http://root@localhost:8000");
		assertEquals("http://localhost:8000", parsed.getNativeConnectString());
		assertEquals("root", parsed.getUsername());
		assertNull(parsed.getPassword());
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
		// with a single trailing slash ("/ns"), which legitimately means "namespace
		// only, no database" (see trailingSlashAfterNamespaceOnlyIsAllowed).
		assertThrows(IllegalArgumentException.class,
				() -> JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/ns//"));
	}

	@Test
	void trailingSlashAfterNamespaceOnlyIsAllowed() {
		// A single trailing slash with no database segment is fine (not the same as
		// an empty database segment): "ns/" means "namespace=ns, no database".
		JdbcConnectionUrl parsed = JdbcConnectionUrl.parse("jdbc:surrealdb://localhost:8000/ns");
		assertEquals("ns", parsed.getNamespace());
		assertNull(parsed.getDatabase());
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
