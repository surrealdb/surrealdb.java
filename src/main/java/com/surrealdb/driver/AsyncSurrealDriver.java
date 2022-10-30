package com.surrealdb.driver;

import com.google.gson.reflect.TypeToken;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.SignIn;
import com.surrealdb.driver.model.patch.Patch;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * An asynchronous SurrealDB driver. This driver is used in conjunction with a
 * {@link SurrealConnection} to communicate with the server. The driver provides methods for
 * signing in to the server executing queries, and subscribing to data (coming soon). All methods in this
 * class are asynchronous and return a {@link CompletableFuture} that will be completed when
 * the operation is finished.
 *
 * @author Khalid Alharisi
 */
public class AsyncSurrealDriver implements SurrealDriver {

    private final SurrealConnection connection;
    private final ExecutorService asyncOperationExecutorService;

    /**
     * Creates a new {@link AsyncSurrealDriver} instance using the provided {@link SurrealConnection}
     * and {@link SurrealDriverSettings}. The connection must connect to a SurrealDB server before
     * any driver methods are called.
     *
     * @param connection The connection to use for communicating with the server.
     * @param settings   The settings this driver should use.
     */
    public AsyncSurrealDriver(SurrealConnection connection, SurrealDriverSettings settings) {
        this.connection = connection;
        this.asyncOperationExecutorService = settings.getAsyncOperationExecutorService();
    }

    /**
     * Creates a new {@link AsyncSurrealDriver} instance using the provided connection. The connection
     * must connect to a SurrealDB server before any driver methods are called.
     *
     * @param connection The connection to use for communication with the server
     */
    public AsyncSurrealDriver(SurrealConnection connection) {
        this(connection, SurrealDriverSettings.DEFAULT);
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
     * @param username  The username to sign in with
     * @param password  The password to sign in with
     * @param namespace The namespace to sign in with
     * @param database  The database to sign in with
     * @param scope     The scope to sign in with
     * @return a {@link CompletableFuture} that will complete with the result of the sign in
     * operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> signIn(String username, String password, String namespace, String database, String scope) {
        return connection.rpc("signin", new SignIn(username, password, namespace, database, scope));
    }

    public CompletableFuture<Void> signIn(String username, String password, String namespace, String database) {
        return connection.rpc("signin", new SignIn(username, password, namespace, database, null));
    }

    /**
     * @param username The username to sign in with
     * @param password The password to sign in with
     * @return a {@link CompletableFuture} that will complete with the result of the sign in
     * operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> signIn(String username, String password) {
        return connection.rpc("signin", new SignIn(username, password, null, null, null));
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

    /**
     * Sets a connection-wide parameter
     *
     * @param key   The parameter to set
     * @param value The value to set the parameter to
     * @return a {@link CompletableFuture} that will complete once the SurrealDB server has
     * responded to the set operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> setConnectionWideParameter(String key, String value) {
        return connection.rpc("let", key, value);
    }

    /**
     * Unset and clear a connection-wide parameter
     *
     * @param key The parameter to unset
     * @return a {@link CompletableFuture} that will complete once the SurrealDB server has
     * responded to the unset operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> unsetConnectionWideParameter(String key) {
        return connection.rpc("unset", key);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> query(String query, Map<String, String> args, Class<? extends T> rowType) {
        Type queryResultType = TypeToken.getParameterized(QueryResult.class, rowType).getType();
        Type resultType = TypeToken.getParameterized(List.class, queryResultType).getType();
        return connection.rpc(resultType, "query", query, args);
    }

    /**
     * Runs the provided query and returns the <i>the first result</i> in the <i>first query</i> wrapped as an optional.
     * This method is just a convenient wrapper around {@link #query(String, Map, Class)} for use cases where only a
     * single result is needed. Therefore, it's recommended to use a limit of 1 in the query to insure only the desired
     * result is transmitted.
     *
     * @param query   The query to execute
     * @param args    The arguments to use in the query
     * @param rowType The type of the rows in the result
     * @param <T>     The type of the rows in the result
     * @return a {@link CompletableFuture} that will complete with an {@link Optional} containing the first result in the query
     * or an empty {@link Optional} if the query returned no results. If an error occurs, the future will complete exceptionally.
     * @see #query(String, Map, Class)
     */
    public <T> CompletableFuture<Optional<T>> querySingle(String query, Map<String, String> args, Class<? extends T> rowType) {
        CompletableFuture<List<QueryResult<T>>> rpcCallback = query(query, args, rowType);

        return rpcCallback.thenApplyAsync(queryResults -> {
            // If there are no query results, return an empty optional
            if (queryResults.isEmpty()) {
                return Optional.empty();
            }
            // Since there is at least one query, it's safe to get the first one
            QueryResult<T> firstQueryResult = queryResults.get(0);
            return getFirstElement(firstQueryResult.getResult());
        }, asyncOperationExecutorService);
    }

    public <T> CompletableFuture<List<T>> select(String thing, Class<? extends T> rowType) {
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return connection.rpc(resultType, "select", thing);
    }

    /**
     * Selects the provided <i>thing</i> and returns the <i>first result</i> wrapped as an optional. This method is just a
     * convenient wrapper around {@link #select(String, Class)} for use cases where only a single result is needed.
     *
     * @param thing   The thing to select
     * @param rowType The type of the result
     * @param <T>     The type of the result
     * @return a {@link CompletableFuture} that will complete with an {@link Optional} containing the first result in the query.
     * If an error occurs, the future will complete exceptionally.
     * @see #select(String, Class)
     */
    public <T> CompletableFuture<Optional<T>> selectSingle(String thing, Class<? extends T> rowType) {
        CompletableFuture<List<T>> result = select(thing, rowType);
        return result.thenApplyAsync(this::getFirstElement, asyncOperationExecutorService);
    }

    public <T> CompletableFuture<T> create(String thing, T data) {
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        CompletableFuture<T> finalFuture = new CompletableFuture<>();

        CompletableFuture<List<T>> createFuture = connection.rpc(resultType, "create", thing, data);
        createFuture.whenCompleteAsync(((list, throwable) -> {
            if (throwable != null) {
                finalFuture.completeExceptionally(throwable);
            } else {
                finalFuture.complete(list.get(0));
            }
        }), asyncOperationExecutorService);

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

    @Override
    public SurrealConnection getSurrealConnection() {
        return connection;
    }

    @Override
    public ExecutorService getAsyncOperationExecutorService() {
        return asyncOperationExecutorService;
    }

    /**
     * @param list The list to get the first element from
     * @param <T>  The type of the list
     * @return the first element in the list or an empty {@link Optional} if the list is empty
     */
    private <T> Optional<T> getFirstElement(List<T> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
