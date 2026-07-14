package com.surrealdb.signin;

import java.util.Objects;

/**
 * Credentials for signing in via a named, user-defined system-level
 * {@code ACCESS} method verified with a username and password — distinct from
 * the built-in root/namespace/database user tables (see
 * {@link RootCredential}/{@link NamespaceCredential}/{@link DatabaseCredential})
 * and from record access (see {@link RecordCredential}). Used with
 * {@link com.surrealdb.Surreal#signin(Credential)}.
 * <p>
 * Structurally this is like {@code RootCredential}/{@code NamespaceCredential}/
 * {@code DatabaseCredential} ({@code username} + {@code password}) plus a
 * mandatory {@code access} method name, mirroring the JS SDK's
 * {@code AccessSystemAuth}.
 * <p>
 * {@code namespace} and {@code database} are sent to the server exactly as
 * given: when null, they are omitted from the request entirely — there is <b>no
 * fallback</b> to the session's current namespace/database set via
 * {@link com.surrealdb.Surreal#useNs(String)} /
 * {@link com.surrealdb.Surreal#useDb(String)}. However, a {@code database}
 * without a {@code namespace} is rejected client-side before any request is
 * made, mirroring the JS SDK's {@code MissingNamespaceDatabaseError}; see
 * {@link com.surrealdb.Surreal#signin(Credential)}.
 */
public class AccessCredential implements Credential {

	private final String namespace;
	private final String database;
	private final String username;
	private final String password;
	private final String access;

	/**
	 * Creates access credentials without an explicit namespace/database (sent
	 * exactly as omitted; no session fallback).
	 *
	 * @param username
	 *            the username to verify
	 * @param password
	 *            the password to verify
	 * @param access
	 *            the access method name (as defined in {@code DEFINE ACCESS})
	 */
	public AccessCredential(String username, String password, String access) {
		this(username, password, access, null, null);
	}

	/**
	 * Creates access credentials scoped to a namespace (no database).
	 *
	 * @param username
	 *            the username to verify
	 * @param password
	 *            the password to verify
	 * @param access
	 *            the access method name (as defined in {@code DEFINE ACCESS})
	 * @param namespace
	 *            namespace name, or null to omit it from the request
	 */
	public AccessCredential(String username, String password, String access, String namespace) {
		this(username, password, access, namespace, null);
	}

	/**
	 * Creates access credentials scoped to a namespace and database.
	 *
	 * @param username
	 *            the username to verify
	 * @param password
	 *            the password to verify
	 * @param access
	 *            the access method name (as defined in {@code DEFINE ACCESS})
	 * @param namespace
	 *            namespace name, or null to omit it from the request
	 * @param database
	 *            database name, or null to omit it from the request (a database
	 *            without a namespace is rejected by
	 *            {@link com.surrealdb.Surreal#signin(Credential)})
	 */
	public AccessCredential(String username, String password, String access, String namespace, String database) {
		this.username = Objects.requireNonNull(username, "username");
		this.password = Objects.requireNonNull(password, "password");
		this.access = Objects.requireNonNull(access, "access");
		this.namespace = namespace;
		this.database = database;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getAccess() {
		return access;
	}
}
