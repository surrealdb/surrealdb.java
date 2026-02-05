package com.surrealdb.signin;

/**
 * Credential that authenticates the session with an existing access token (e.g. JWT).
 * Use with {@link com.surrealdb.Surreal#signin(Credential)} to set the session to this token
 * without performing a new sign-in request.
 */
public class Bearer implements Credential {

    private final String token;

    /**
     * Creates a bearer credential with the given access token.
     *
     * @param token the access token (JWT) to use for authentication
     */
    public Bearer(String token) {
        this.token = token;
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
