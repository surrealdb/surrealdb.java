package com.surrealdb.connection;

import org.jetbrains.annotations.NotNull;

/**
 * An enum containing protocols that can be used to connect to a SurrealDB server.
 */
public enum SurrealConnectionProtocol {

    /**
     * WebSocket without encryption. Uses <b>ws://</b> as the protocol scheme.
     */
    WEB_SOCKET("ws"),

    /**
     * WebSocket over SSL. Uses <b>wss://</b> as the protocol scheme.
     */
    WEB_SOCKET_SSL("wss");

    @NotNull String scheme;

    SurrealConnectionProtocol(@NotNull String scheme) {
        this.scheme = scheme;
    }

    /**
     * Returns the scheme of this protocol. This is used to construct the URI. For example, the
     * scheme of {@link #WEB_SOCKET} is {@code ws}. When constructing a URI, the scheme is followed
     * by a colon and two slashes.
     *
     * @return The scheme of this protocol.
     */
    public @NotNull String getScheme() {
        return scheme;
    }
}
