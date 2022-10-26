package com.surrealdb.driver;

import com.google.gson.reflect.TypeToken;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.SignIn;
import com.surrealdb.driver.model.patch.Patch;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * An asynchronous SurrealDB driver. This driver is used in conjunction with a
 * {@link SurrealConnection} to communicate with the server. The driver provides methods for
 * signing in to the server executing queries, and subscribing to data (coming soon). All methods in this
 * class are asynchronous and return a {@link CompletableFuture} that will be completed when
 * the operation is finished.
 *
 * @author Khalid Alharisi
 */
public class AsyncSurrealDriver {

    private final SurrealConnection connection;

    /**
     * Creates a new {@link AsyncSurrealDriver} instance using the provided connection. The connection
     * must connect to a SurrealDB server before any driver methods are called.
     *
     * @param connection The connection to use for communication with the server
     */
    public AsyncSurrealDriver(SurrealConnection connection) {
        this.connection = connection;
    }

    /**
     * @return a {@link CompletableFuture} that will complete once the SurrealDB server has
     * responded to the ping. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> ping() {
        return connection.rpc("ping");
    }

    public CompletableFuture<Map<String, String>> info() {
        Type resultType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();
        return connection.rpc(resultType, "info");
    }

    /**
     * @param username The username to sign in with
     * @param password The password to sign in with
     * @return a {@link CompletableFuture} that will complete with the result of the sign in
     * operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> signIn(String username, String password) {
        return connection.rpc("signin", new SignIn(username, password));
    }

    /**
     * @param namespace The namespace to use
     * @param database  The database to use
     * @return a {@link CompletableFuture} that will complete once the SurrealDB server has
     * responded to the use operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> use(String namespace, String database) {
        return connection.rpc("use", namespace, database);
    }

    public CompletableFuture<Void> let(String key, String value) {
        return connection.rpc("let", key, value);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> query(String query, Map<String, String> args, Class<? extends T> rowType) {
        Type queryResultType = TypeToken.getParameterized(QueryResult.class, rowType).getType();
        Type resultType = TypeToken.getParameterized(List.class, queryResultType).getType();
        return connection.rpc(resultType, "query", query, args);
    }

    public <T> CompletableFuture<List<T>> select(String thing, Class<? extends T> rowType) {
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return connection.rpc(resultType, "select", thing);
    }

    public <T> CompletableFuture<T> create(String thing, T data) {
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        CompletableFuture<T> finalFuture = new CompletableFuture<>();

        CompletableFuture<List<T>> createFuture = connection.rpc(resultType, "create", thing, data);
        createFuture.whenComplete((list, throwable) -> {
            if (throwable != null) {
                finalFuture.completeExceptionally(throwable);
            } else {
                finalFuture.complete(list.get(0));
            }
        });

        return finalFuture;
    }

    public <T> CompletableFuture<List<T>> update(String thing, T data) {
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        return connection.rpc(resultType, "update", thing, data);
    }

    public <T, P> CompletableFuture<List<T>> change(String thing, P data, Class<T> rowType) {
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return connection.rpc(resultType, "change", thing, data);
    }

    public CompletableFuture<?> patch(String thing, List<Patch> patches) {
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return connection.rpc(resultType, "modify", thing, patches);
    }

    public CompletableFuture<?> delete(String thing) {
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return connection.rpc(resultType, "delete", thing);
    }
}
