package com.surrealdb.connection;

/**
 * @author Damian Kocher
 */
public enum SurrealConnectionProtocol {

    /**
     * WebSocket protocol.
     */
    WEB_SOCKET("ws"),

    /**
     * Secure WebSocket protocol.
     */
    WEB_SOCKET_SECURE("wss");

    private final String scheme;

    SurrealConnectionProtocol(String scheme) {
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }
}
