package com.surrealdb.connection;

public enum SurrealConnectionProtocol {

    WEB_SOCKET("ws"),
    WEB_SOCKET_SECURE("wss");

    private final String scheme;

    SurrealConnectionProtocol(String scheme) {
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }
}
