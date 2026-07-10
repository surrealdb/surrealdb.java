package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
