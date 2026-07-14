package com.surrealdb.signin;

import java.util.Objects;

/**
 * Credentials for signing in with a pre-issued {@code TYPE BEARER} access grant
 * key, e.g. from:
 *
 * <pre>
 * DEFINE ACCESS api_key ON DATABASE TYPE BEARER FOR USER DURATION FOR TOKEN 15m, FOR SESSION 12h;
 * ACCESS api_key ON DATABASE GRANT FOR USER someuser;
 * </pre>
 *
 * which issues a key of the form {@code surreal-bearer-<id>-<random>}. Used
 * with {@link com.surrealdb.Surreal#signin(Credential)}.
 * <p>
 * {@code namespace} and {@code database} are sent to the server exactly as
 * given: when null, they are omitted from the request entirely. Unlike
 * {@link RecordCredential}, there is <b>no fallback</b> to the session's
 * current namespace/database set via
 * {@link com.surrealdb.Surreal#useNs(String)} /
 * {@link com.surrealdb.Surreal#useDb(String)} — this matches the JS SDK's
 * {@code AccessBearerAuth}, which never falls back to session state either.
 * <p>
 * <b>Not to be confused with {@link BearerCredential}</b>, despite the similar
 * name: {@code AccessKeyCredential} signs in with a bearer <em>grant key</em>
 * via the {@code signin} RPC, verified server-side against a
 * {@code TYPE BEARER} access method. {@link BearerCredential} instead
 * reattaches an already-issued JWT to the session via the {@code authenticate}
 * RPC — no new sign-in request is made, and no access method is involved.
 */
public class AccessKeyCredential implements Credential {

	private final String namespace;
	private final String database;
	private final String access;
	private final String key;

	/**
	 * Creates access-key credentials without an explicit namespace/database (sent
	 * exactly as omitted; no session fallback).
	 *
	 * @param access
	 *            the {@code TYPE BEARER} access method name (as defined in
	 *            {@code DEFINE ACCESS})
	 * @param key
	 *            the bearer grant key (as issued by
	 *            {@code ACCESS ... GRANT FOR USER ...})
	 */
	public AccessKeyCredential(String access, String key) {
		this(null, null, access, key);
	}

	/**
	 * Creates access-key credentials with an explicit namespace and database.
	 *
	 * @param namespace
	 *            namespace name, or null to omit it from the request
	 * @param database
	 *            database name, or null to omit it from the request
	 * @param access
	 *            the {@code TYPE BEARER} access method name (as defined in
	 *            {@code DEFINE ACCESS})
	 * @param key
	 *            the bearer grant key (as issued by
	 *            {@code ACCESS ... GRANT FOR USER ...})
	 */
	public AccessKeyCredential(String namespace, String database, String access, String key) {
		this.namespace = namespace;
		this.database = database;
		this.access = Objects.requireNonNull(access, "access");
		this.key = Objects.requireNonNull(key, "key");
	}

	public String getNamespace() {
		return namespace;
	}

	public String getDatabase() {
		return database;
	}

	public String getAccess() {
		return access;
	}

	public String getKey() {
		return key;
	}
}
