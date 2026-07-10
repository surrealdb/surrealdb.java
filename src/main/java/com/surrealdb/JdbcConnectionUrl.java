package com.surrealdb;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

/**
 * Parses a JDBC-style connection URL for SurrealDB into the pieces needed to
 * drive {@link Surreal#connect(String)}: the native connect string understood
 * by the underlying engine, plus any namespace, database, username, and
 * password embedded in the URL.
 * <p>
 * The grammar follows the standard JDBC convention of
 * {@code jdbc:<subprotocol>:<subname>}, with {@code surrealdb} as the
 * subprotocol:
 * <ul>
 * <li>Remote engines:
 * {@code jdbc:surrealdb:[scheme://][user[:password]@]host[:port][/namespace[/database]][?param=value&...]}
 * where {@code scheme} is one of {@code ws}, {@code wss}, {@code http}, or
 * {@code https} (defaulting to {@code ws} when omitted, e.g.
 * {@code jdbc:surrealdb://localhost:8000}). Recognized query parameters are
 * {@code user}/{@code username}, {@code pass}/{@code password},
 * {@code ns}/{@code namespace}, and {@code db}/{@code database}. When a field
 * is supplied both in the userinfo/path and in the query string, the
 * userinfo/path value wins (field by field, so one source may complete the
 * other, e.g. {@code jdbc:surrealdb://root@host?pass=secret}).</li>
 * <li>Embedded memory engine: {@code jdbc:surrealdb:memory[?param=value&...]}
 * or {@code jdbc:surrealdb:mem://...[?param=value&...]}, using the same query
 * parameter names as above.</li>
 * <li>Any other embedded/file/cluster engine (e.g. {@code rocksdb://},
 * {@code surrealkv://}, {@code tikv://}, {@code fdb://}, {@code indxdb://}):
 * the connect string is passed through unchanged (it may contain a filesystem
 * path), other than stripping an optional trailing {@code ?param=value&...}
 * suffix using the same query parameter names as above.</li>
 * </ul>
 * Percent-encoded octets ({@code %XX}, including multi-byte UTF-8) are decoded
 * in the userinfo, path segments, and query values. A literal {@code '+'} is
 * preserved in the userinfo and path (per RFC 3986) but decodes to a space in
 * query values (per {@code application/x-www-form-urlencoded}); write
 * {@code %2B} for a literal plus in a query value.
 * <p>
 * After merging both sources, half-specified combinations are rejected with an
 * {@link IllegalArgumentException} rather than silently dropped: a username
 * requires a password (and vice versa), and a database requires a namespace. An
 * empty userinfo ({@code jdbc:surrealdb://@host}) is treated as absent, not as
 * empty credentials.
 * <p>
 * This class is package-private: it is an implementation detail of
 * {@link Surreal#connect(String)} and is not part of the public API.
 */
final class JdbcConnectionUrl {

	/** The required prefix for every JDBC-style connection URL, lowercase. */
	private static final String JDBC_PREFIX = "jdbc:surrealdb:";

	private final String nativeConnectString;
	private final String namespace;
	private final String database;
	private final String username;
	private final String password;

	private JdbcConnectionUrl(String nativeConnectString, String namespace, String database, String username,
			String password) {
		this.nativeConnectString = nativeConnectString;
		this.namespace = namespace;
		this.database = database;
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns the connection string to hand to the native {@code connect()} call
	 * (e.g. {@code "ws://localhost:8000"}, {@code "memory"}, or
	 * {@code "surrealkv:///some/path"}).
	 *
	 * @return the native connect string
	 */
	String getNativeConnectString() {
		return nativeConnectString;
	}

	/**
	 * Returns the namespace embedded in the URL, if any.
	 *
	 * @return the namespace, or null
	 */
	String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the database embedded in the URL, if any.
	 *
	 * @return the database, or null
	 */
	String getDatabase() {
		return database;
	}

	/**
	 * Returns the username embedded in the URL, if any.
	 *
	 * @return the username, or null
	 */
	String getUsername() {
		return username;
	}

	/**
	 * Returns the password embedded in the URL, if any.
	 *
	 * @return the password, or null
	 */
	String getPassword() {
		return password;
	}

	/**
	 * Parses a JDBC-style connection URL for SurrealDB.
	 *
	 * @param url
	 *            the URL to parse, e.g.
	 *            {@code "jdbc:surrealdb://root:root@localhost:8000/test/test"}
	 * @return the parsed connection information
	 * @throws IllegalArgumentException
	 *             if the URL is malformed or does not start with
	 *             {@code "jdbc:surrealdb:"}
	 */
	static JdbcConnectionUrl parse(String url) {
		if (url == null) {
			throw new IllegalArgumentException("JDBC connection URL must not be null");
		}
		if (url.length() < JDBC_PREFIX.length()
				|| !url.substring(0, JDBC_PREFIX.length()).toLowerCase(Locale.ROOT).equals(JDBC_PREFIX)) {
			throw new IllegalArgumentException(
					"Invalid JDBC connection URL (must start with \"jdbc:surrealdb:\"): " + url);
		}
		final String rest = url.substring(JDBC_PREFIX.length());
		final String restLower = rest.toLowerCase(Locale.ROOT);

		final JdbcConnectionUrl parsed;
		if (startsWithIgnoreCase(restLower, "ws://") || startsWithIgnoreCase(restLower, "wss://")
				|| startsWithIgnoreCase(restLower, "http://") || startsWithIgnoreCase(restLower, "https://")) {
			parsed = parseRemote(url, rest);
		} else if (restLower.startsWith("//")) {
			// "jdbc:surrealdb://host..." with no explicit scheme token defaults to
			// ws://, mirroring the common JDBC convention where the subprotocol name
			// doubles as the transport (e.g. "jdbc:postgresql://host/db").
			parsed = parseRemote(url, "ws:" + rest);
		} else if (restLower.equals("memory") || startsWithIgnoreCase(restLower, "mem://")) {
			parsed = parseMemory(rest);
		} else {
			parsed = parseEmbedded(rest);
		}

		// Validate cross-field consistency after the userinfo/path and query-string
		// sources have been merged, so that one source may legitimately complete the
		// other (e.g. "ws://root@host?pass=secret"). Half-specified combinations are
		// rejected here rather than silently dropped or passed through: a username
		// without a password would fail at sign-in with an unhelpful native error,
		// a password alone can never be used, and SurrealDB cannot select a
		// database without a namespace.
		if (parsed.username != null && parsed.password == null) {
			throw new IllegalArgumentException(
					"Invalid JDBC connection URL (username supplied without a password): " + url);
		}
		if (parsed.password != null && parsed.username == null) {
			throw new IllegalArgumentException(
					"Invalid JDBC connection URL (password supplied without a username): " + url);
		}
		if (parsed.database != null && parsed.namespace == null) {
			throw new IllegalArgumentException(
					"Invalid JDBC connection URL (database supplied without a namespace): " + url);
		}
		return parsed;
	}

	private static boolean startsWithIgnoreCase(String lowerCaseHaystack, String lowerCaseNeedle) {
		return lowerCaseHaystack.startsWith(lowerCaseNeedle);
	}

	/**
	 * Parses a remote-engine URL: {@code scheme://[user[:password]@]host[:port]
	 * [/namespace[/database]][?params]}. The native connect string produced is just
	 * {@code scheme://host[:port]}, with no userinfo, path, or query.
	 *
	 * @param url
	 *            the original, unmodified URL as passed to {@link #parse(String)},
	 *            used only to build accurate error messages (the {@code rest}
	 *            parameter may have an implicit {@code ws:} scheme prepended and
	 *            would otherwise produce a confusing error message).
	 * @param rest
	 *            everything after the {@code "jdbc:surrealdb:"} prefix
	 */
	private static JdbcConnectionUrl parseRemote(String url, String rest) {
		final int schemeEnd = rest.indexOf("://");
		// Normalized to lowercase: the native "any" engine dispatch is case-sensitive
		// (it matches on literal lowercase tokens such as "ws"/"http"), so passing
		// through a differently-cased scheme (e.g. "WS://...") would cause the native
		// side to fail to recognize it and reject the connection, even though this
		// parser itself recognizes scheme tokens case-insensitively.
		final String scheme = rest.substring(0, schemeEnd).toLowerCase(Locale.ROOT);
		String remainder = rest.substring(schemeEnd + 3);

		// Split off the query string first, if any.
		String query = null;
		final int queryIdx = remainder.indexOf('?');
		if (queryIdx >= 0) {
			query = remainder.substring(queryIdx + 1);
			remainder = remainder.substring(0, queryIdx);
		}

		// Split off the path, if any.
		String path = null;
		final int pathIdx = remainder.indexOf('/');
		if (pathIdx >= 0) {
			path = remainder.substring(pathIdx + 1);
			remainder = remainder.substring(0, pathIdx);
		}

		// remainder is now "[user[:password]@]host[:port]".
		String userinfoUser = null;
		String userinfoPassword = null;
		final int atIdx = remainder.lastIndexOf('@');
		String hostPort = remainder;
		if (atIdx >= 0) {
			final String userinfo = remainder.substring(0, atIdx);
			hostPort = remainder.substring(atIdx + 1);
			// An empty userinfo ("@host" or ":@host") carries no credentials:
			// normalize it to null (no sign-in attempt) rather than treating it as
			// user "" with password "".
			if (!userinfo.isEmpty() && !userinfo.equals(":")) {
				final int colonIdx = userinfo.indexOf(':');
				if (colonIdx >= 0) {
					final String user = percentDecode(userinfo.substring(0, colonIdx));
					// ":pass@host" supplies a password but no username; leave the
					// username null so parse() rejects it unless the query string
					// fills it in.
					userinfoUser = user.isEmpty() ? null : user;
					userinfoPassword = percentDecode(userinfo.substring(colonIdx + 1));
				} else {
					userinfoUser = percentDecode(userinfo);
				}
			}
		}
		if (hostPort.isEmpty()) {
			throw new IllegalArgumentException("Invalid JDBC connection URL (missing host): " + url);
		}

		String pathNamespace = null;
		String pathDatabase = null;
		if (path != null && !path.isEmpty()) {
			final String[] segments = path.split("/", -1);
			// Drop a single trailing empty segment caused by a trailing slash.
			final int segmentCount = (segments.length > 0 && segments[segments.length - 1].isEmpty())
					? segments.length - 1
					: segments.length;
			if (segmentCount > 2) {
				throw new IllegalArgumentException(
						"Invalid JDBC connection URL (at most two path segments, namespace/database, are allowed): "
								+ url);
			}
			if (segmentCount >= 1) {
				pathNamespace = percentDecode(segments[0]);
				if (pathNamespace.isEmpty()) {
					throw new IllegalArgumentException(
							"Invalid JDBC connection URL (namespace path segment must not be empty): " + url);
				}
			}
			if (segmentCount >= 2) {
				pathDatabase = percentDecode(segments[1]);
				if (pathDatabase.isEmpty()) {
					throw new IllegalArgumentException(
							"Invalid JDBC connection URL (database path segment must not be empty): " + url);
				}
			}
		}

		final QueryParams queryParams = QueryParams.parse(query);

		final String username = firstNonNull(userinfoUser, queryParams.user);
		final String password = firstNonNull(userinfoPassword, queryParams.pass);
		final String namespace = firstNonNull(pathNamespace, queryParams.ns);
		final String database = firstNonNull(pathDatabase, queryParams.db);

		final String nativeConnectString = scheme + "://" + hostPort;
		return new JdbcConnectionUrl(nativeConnectString, namespace, database, username, password);
	}

	/**
	 * Parses the embedded memory engine: {@code memory[?params]} or
	 * {@code mem://...[?params]}. The native connect string is always exactly
	 * {@code "memory"}.
	 */
	private static JdbcConnectionUrl parseMemory(String rest) {
		String remainder = rest;
		String query = null;
		final int queryIdx = remainder.indexOf('?');
		if (queryIdx >= 0) {
			query = remainder.substring(queryIdx + 1);
			remainder = remainder.substring(0, queryIdx);
		}
		final QueryParams queryParams = QueryParams.parse(query);
		return new JdbcConnectionUrl("memory", queryParams.ns, queryParams.db, queryParams.user, queryParams.pass);
	}

	/**
	 * Parses any other embedded/file/cluster engine. Only an optional trailing
	 * {@code ?params} suffix is stripped; the remaining string is passed through
	 * unchanged as the native connect string since it may contain a filesystem
	 * path.
	 */
	private static JdbcConnectionUrl parseEmbedded(String rest) {
		if (rest.isEmpty()) {
			throw new IllegalArgumentException(
					"Invalid JDBC connection URL (empty connect string): jdbc:surrealdb:" + rest);
		}
		String remainder = rest;
		String query = null;
		final int queryIdx = remainder.indexOf('?');
		if (queryIdx >= 0) {
			query = remainder.substring(queryIdx + 1);
			remainder = remainder.substring(0, queryIdx);
		}
		final QueryParams queryParams = QueryParams.parse(query);
		return new JdbcConnectionUrl(remainder, queryParams.ns, queryParams.db, queryParams.user, queryParams.pass);
	}

	private static String firstNonNull(String preferred, String fallback) {
		return preferred != null ? preferred : fallback;
	}

	/**
	 * Decodes an {@code application/x-www-form-urlencoded} query-string value:
	 * {@code %XX} octets are expanded and {@code '+'} means space. Only correct for
	 * query values - use {@link #percentDecode} for userinfo and path components.
	 */
	private static String decode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is always supported; this should be unreachable.
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Decodes percent-encoded octets ({@code %XX}, including multi-byte UTF-8
	 * sequences) in a URI userinfo or path component. Unlike {@link #decode}, a
	 * literal {@code '+'} is preserved: the plus-means-space rule is specific to
	 * form-encoded query values and does not apply to these components (RFC 3986),
	 * where {@code '+'} is an ordinary character - and a common one in passwords.
	 */
	private static String percentDecode(String value) {
		// Re-encoding literal '+' as %2B first lets URLDecoder handle the actual
		// %XX (and multi-byte UTF-8) expansion while leaving pluses intact.
		return decode(value.replace("+", "%2B"));
	}

	/**
	 * A tiny holder for the recognized query-string parameters: {@code user} (alias
	 * {@code username}), {@code pass} (alias {@code password}), {@code ns} (alias
	 * {@code namespace}), and {@code db} (alias {@code database}).
	 */
	private static final class QueryParams {

		private String user;
		private String pass;
		private String ns;
		private String db;

		static QueryParams parse(String query) {
			final QueryParams params = new QueryParams();
			if (query == null || query.isEmpty()) {
				return params;
			}
			for (final String pair : query.split("&")) {
				if (pair.isEmpty()) {
					continue;
				}
				final int eqIdx = pair.indexOf('=');
				final String key = eqIdx >= 0 ? pair.substring(0, eqIdx) : pair;
				final String value = eqIdx >= 0 ? decode(pair.substring(eqIdx + 1)) : "";
				switch (key.toLowerCase(Locale.ROOT)) {
					case "user" :
					case "username" :
						params.user = value;
						break;
					case "pass" :
					case "password" :
						params.pass = value;
						break;
					case "ns" :
					case "namespace" :
						params.ns = value;
						break;
					case "db" :
					case "database" :
						params.db = value;
						break;
					default :
						// Unrecognized query parameters are ignored.
						break;
				}
			}
			return params;
		}
	}
}
