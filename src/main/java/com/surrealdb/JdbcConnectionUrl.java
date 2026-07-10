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
 * userinfo/path value wins.</li>
 * <li>Embedded memory engine: {@code jdbc:surrealdb:memory[?param=value&...]}
 * or {@code jdbc:surrealdb:mem://...[?param=value&...]}, using the same query
 * parameter names as above.</li>
 * <li>Any other embedded/file/cluster engine (e.g. {@code rocksdb://},
 * {@code surrealkv://}, {@code tikv://}, {@code fdb://}, {@code indxdb://}):
 * the connect string is passed through unchanged (it may contain a filesystem
 * path), other than stripping an optional trailing {@code ?param=value&...}
 * suffix using the same query parameter names as above.</li>
 * </ul>
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

		if (startsWithIgnoreCase(restLower, "ws://") || startsWithIgnoreCase(restLower, "wss://")
				|| startsWithIgnoreCase(restLower, "http://") || startsWithIgnoreCase(restLower, "https://")) {
			return parseRemote(rest);
		}
		// "jdbc:surrealdb://host..." with no explicit scheme token defaults to ws://,
		// mirroring the common JDBC convention where the subprotocol name doubles as
		// the transport (e.g. "jdbc:postgresql://host/db").
		if (restLower.startsWith("//")) {
			return parseRemote("ws:" + rest);
		}
		if (restLower.equals("memory") || startsWithIgnoreCase(restLower, "mem://")) {
			return parseMemory(rest);
		}
		return parseEmbedded(rest);
	}

	private static boolean startsWithIgnoreCase(String lowerCaseHaystack, String lowerCaseNeedle) {
		return lowerCaseHaystack.startsWith(lowerCaseNeedle);
	}

	/**
	 * Parses a remote-engine URL: {@code scheme://[user[:password]@]host[:port]
	 * [/namespace[/database]][?params]}. The native connect string produced is just
	 * {@code scheme://host[:port]}, with no userinfo, path, or query.
	 */
	private static JdbcConnectionUrl parseRemote(String rest) {
		final int schemeEnd = rest.indexOf("://");
		final String scheme = rest.substring(0, schemeEnd);
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
			final int colonIdx = userinfo.indexOf(':');
			if (colonIdx >= 0) {
				userinfoUser = decode(userinfo.substring(0, colonIdx));
				userinfoPassword = decode(userinfo.substring(colonIdx + 1));
			} else {
				userinfoUser = decode(userinfo);
			}
		}
		if (hostPort.isEmpty()) {
			throw new IllegalArgumentException("Invalid JDBC connection URL (missing host): jdbc:surrealdb:" + rest);
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
						"Invalid JDBC connection URL (at most two path segments, namespace/database, are allowed): jdbc:surrealdb:"
								+ rest);
			}
			if (segmentCount >= 1) {
				pathNamespace = decode(segments[0]);
			}
			if (segmentCount >= 2) {
				pathDatabase = decode(segments[1]);
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

	private static String decode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// UTF-8 is always supported; this should be unreachable.
			throw new IllegalStateException(e);
		}
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
