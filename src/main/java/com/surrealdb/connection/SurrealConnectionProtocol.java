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

    /**
     * Returns the scheme of this protocol. This is used to construct the URI. For example, the
     * scheme of {@link #WEB_SOCKET} is {@code ws}. When constructing a URI, the scheme is followed
     * by a colon and two slashes.
     *
     * @return The scheme of this protocol.
     */
    public String getScheme() {
        return scheme;
    }
}
