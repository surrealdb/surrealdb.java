package com.surrealdb.connection;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * @author Khalid Alharisi
 */
public interface SurrealConnection {

    static SurrealConnection create(SurrealConnectionSettings settings) {
        SurrealConnection surrealConnection = new SurrealWebSocketConnection(settings);

        if (settings.isAutoConnect()) {
            surrealConnection.connect(settings.getDefaultConnectTimeoutSeconds());
        }

        return surrealConnection;
    }

    static SurrealConnection create(SurrealConnectionProtocol protocol, String host, int port) {
        SurrealConnectionSettings settings = SurrealConnectionSettings.builder()
            .setUriFromComponents(protocol, host, port)
            .build();

        return create(settings);
    }

    void connect(int timeoutSeconds);

    void disconnect();

    <T> CompletableFuture<T> rpc(Type resultType, String method, Object... params);
}
