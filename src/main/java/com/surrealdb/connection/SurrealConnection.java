package com.surrealdb.connection;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * @author Khalid Alharisi
 */
@ParametersAreNonnullByDefault
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

    <T> CompletableFuture<T> rpc(@Nullable Type resultType, String method, Object... params);

    default CompletableFuture<Void> rpc(String method, Object... params) {
        return rpc(null, method, params);
    }
}
