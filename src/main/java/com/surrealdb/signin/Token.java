package com.surrealdb.signin;

/**
 * Represents a session token.
 * This class is used to encapsulate a token string which can be used for authentication purposes.
 */
public class Token {

    private final String token;

    public Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
