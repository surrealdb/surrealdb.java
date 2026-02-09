package com.surrealdb.signin;

/**
 * Credentials for signing up or signing in as a record user (record access
 * method). Used with {@link com.surrealdb.Surreal#signup(RecordCredential)} and
 * {@link com.surrealdb.Surreal#signin(Credential)}.
 * <p>
 * When {@code namespace} or {@code database} are null, the SDK uses the current
 * session namespace/database set via
 * {@link com.surrealdb.Surreal#useNs(String)} and
 * {@link com.surrealdb.Surreal#useDb(String)} (or the values returned by
 * {@link com.surrealdb.Surreal#useNs(String)} etc.). Call those first when
 * omitting ns/db.
 * <p>
 * The params object is serialized and sent as the signup/signin variables (e.g.
 * email, password). It must be a type that can be converted to a SurrealDB
 * value (e.g. Map, POJO, or primitive).
 */
public class RecordCredential implements Credential {

	private final String namespace;
	private final String database;
	private final String access;
	private final Object params;

	/**
	 * Creates record credentials with explicit namespace and database.
	 *
	 * @param namespace
	 *            namespace name (may be null to use session default)
	 * @param database
	 *            database name (may be null to use session default)
	 * @param access
	 *            access method name (as defined in DEFINE ACCESS)
	 * @param params
	 *            signup/signin variables (e.g. map with "email", "password"); will
	 *            be serialized
	 */
	public RecordCredential(String namespace, String database, String access, Object params) {
		this.namespace = namespace;
		this.database = database;
		this.access = access;
		this.params = params;
	}

	/**
	 * Creates record credentials using the current session namespace and database.
	 * Requires {@link com.surrealdb.Surreal#useNs(String)} and
	 * {@link com.surrealdb.Surreal#useDb(String)} (or
	 * {@link com.surrealdb.Surreal#useDefaults()}) to have been called first.
	 *
	 * @param access
	 *            access method name (as defined in DEFINE ACCESS)
	 * @param params
	 *            signup/signin variables (e.g. map with "email", "password"); will
	 *            be serialized
	 */
	public RecordCredential(String access, Object params) {
		this.namespace = null;
		this.database = null;
		this.access = access;
		this.params = params;
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

	public Object getParams() {
		return params;
	}
}
