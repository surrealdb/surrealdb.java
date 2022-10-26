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

    /**
     * The preferred way to create a connection to a SurrealDB server. The provided settings
     * will be used to configure the connection.
     * <p>
     * Currently, this method only returns an instance of {@link SurrealWebSocketConnection}. However,
     * future version may return other implementations of {@link SurrealConnection} depending on the
     * provided settings.
     *
     * @param settings the connection settings to use
     * @return a new {@link SurrealConnection} instance
     */
    static SurrealConnection create(SurrealConnectionSettings settings) {
        return new SurrealWebSocketConnection(settings);
    }

    /**
     * The easiest way to set up a connection to a SurrealDB server. This method will create a
     * connection using the provided protocol, host, and port. All other settings will be left
     * at their default values.
     *
     * @param protocol The protocol to use
     * @param host     The host to connect to
     * @param port     The port to connect to
     * @return a new {@link SurrealConnection} instance
     */
    static SurrealConnection create(SurrealConnectionProtocol protocol, String host, int port) {
        SurrealConnectionSettings settings = SurrealConnectionSettings.builder()
            .setUriFromComponents(protocol, host, port)
            .build();

        return create(settings);
    }

    /**
     * Establishes a connection to the SurrealDB server. This method will block until the connection
     * is established or an error occurs.
     *
     * @param timeoutSeconds The timeout in seconds
     */
    void connect(int timeoutSeconds);

    /**
     * Disconnects from the SurrealDB server. This method will block until the connection is
     * dissolved, or an error occurs.
     */
    void disconnect();

    /**
     * @param resultType The expected result type
     * @param method     The RPC method to call
     * @param params     The parameters to pass to the method
     * @return A {@link CompletableFuture} that will be completed with the result of the RPC call,
     * or an exception if the call fails
     */
    <T> CompletableFuture<T> rpc(@Nullable Type resultType, String method, Object... params);

    /**
     * Sends an RPC call to the SurrealDB server without expecting a return value.
     *
     * @param method The RPC method to call
     * @param params The parameters to pass to the method
     * @return A {@link CompletableFuture} that will be completed SurrealDB responds to the RPC call,
     * or an exception if the call fails
     */
    default CompletableFuture<Void> rpc(String method, Object... params) {
        return rpc(null, method, params);
    }
}
