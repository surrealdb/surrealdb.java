package com.surrealdb.signin;

/**
 * Authentication tokens returned by signin and signup.
 * Contains an access token (required) and an optional refresh token.
 * When the server returns only a single JWT, it is exposed as the access token and refresh is null.
 *
 * @see com.surrealdb.Surreal#signin(Signin)
 * @see com.surrealdb.Surreal#signup(RecordCredential)
 */
public class Token {

    private final String access;
    private final String refresh;

    /**
     * Creates a token with access and optional refresh token.
     *
     * @param access  the access token (JWT), must not be null
     * @param refresh the refresh token, or null if not provided by the server
     */
    public Token(String access, String refresh) {
        this.access = access;
        this.refresh = refresh;
    }

    /**
     * Legacy constructor: single token string as access token, no refresh.
     *
     * @param token the access token
     */
    public Token(String token) {
        this.access = token;
        this.refresh = null;
    }

    /**
     * Returns the access token (JWT) used for authenticating requests.
     *
     * @return the access token
     */
    public String getAccess() {
        return access;
    }

    /**
     * Returns the refresh token if the server provided one, otherwise null.
     *
     * @return the refresh token or null
     */
    public String getRefresh() {
        return refresh;
    }

    /**
     * Returns the access token. Kept for backward compatibility; prefer {@link #getAccess()}.
     *
     * @return the access token
     */
    public String getToken() {
        return access;
    }
}
