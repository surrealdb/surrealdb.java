package com.surrealdb.signin;

/**
 * Root-level credentials (username and password) for signing into SurrealDB.
 * Implements {@link Signin} and {@link Credential}.
 */
public class RootCredential implements Signin {

	private final String username;
	private final String password;

	public RootCredential(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
