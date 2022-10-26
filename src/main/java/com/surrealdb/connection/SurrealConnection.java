package com.surrealdb.connection;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * @author Khalid Alharisi
 */
public interface SurrealConnection {

    void connect(int timeoutSeconds);

    void disconnect();

    <T> CompletableFuture<T> rpc(Type resultType, String method, Object... params);

    static SurrealConnection create(SurrealConnectionSettings settings) {
        SurrealConnection surrealConnection = new SurrealWebSocketConnection(settings);

        if (settings.isAutoConnect()) {
            surrealConnection.connect(settings.getDefaultConnectTimeoutSeconds());
        }

        return surrealConnection;
    }

    static SurrealConnection create(String host, int port, boolean useTls) {
        SurrealConnectionSettings settings = SurrealConnectionSettings.builder()
            .setUriFromComponents(host, port, useTls)
            .build();

        return create(settings);
    }
}
