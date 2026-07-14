package com.surrealdb.signin;

import java.util.Objects;

/**
 * Credential that authenticates the session with an existing access token (e.g.
 * JWT). Use with {@link com.surrealdb.Surreal#signin(Credential)} to set the
 * session to this token without performing a new sign-in request.
 * <p>
 * <b>Not to be confused with {@link AccessKeyCredential}</b>, despite the
 * similar name: {@code BearerCredential} reattaches an already-issued JWT to
 * the session via the {@code authenticate} RPC (the token is trusted as-is; no
 * new sign-in request is made, and no access method is involved).
 * {@link AccessKeyCredential} instead signs in with a pre-issued
 * {@code TYPE BEARER} access grant key (e.g.
 * {@code surreal-bearer-<id>-<random>}) via the {@code signin} RPC, which the
 * server verifies against a {@code TYPE BEARER} access method.
 */
public class BearerCredential implements Credential {

	private final String token;

	/**
	 * Creates a bearer credential with the given access token.
	 *
	 * @param token
	 *            the access token (JWT) to use for authentication; must not be null
	 */
	public BearerCredential(String token) {
		this.token = Objects.requireNonNull(token, "token");
	}

	/**
	 * Returns the access token.
	 *
	 * @return the access token
	 */
	public String getToken() {
		return token;
	}
}
