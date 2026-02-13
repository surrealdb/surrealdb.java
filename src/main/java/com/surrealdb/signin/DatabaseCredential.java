package com.surrealdb.signin;

/**
 * Database-level credentials for signing into a SurrealDB database. Extends
 * {@link NamespaceCredential} and adds a database reference.
 */
public class DatabaseCredential extends NamespaceCredential implements Signin {

	private final String database;

	public DatabaseCredential(String username, String password, String namespace, String database) {
		super(username, password, namespace);
		this.database = database;
	}

	public String getDatabase() {
		return database;
	}
}
