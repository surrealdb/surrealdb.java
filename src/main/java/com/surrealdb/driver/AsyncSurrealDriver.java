package com.surrealdb.driver;

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealExceptionUtils;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.model.QueryResult;
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
    private final ExecutorService executorService;

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
        this.executorService = settings.getAsyncOperationExecutorService();
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
        return connection.rpc(executorService,"ping");
    }

    public CompletableFuture<String> getDatabaseVersion() {
        return connection.rpc(executorService,"version", String.class);
    }

    public CompletableFuture<Map<String, String>> info() {
        Type resultType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();
        return connection.rpc(executorService,"info", resultType);
    }

    public CompletableFuture<Void> signIn(SurrealAuthCredentials credentials) {
        return connection.rpc(executorService,"signin", credentials);
    }

    /**
     * @param namespace The namespace to use
     * @param database  The database to use
     * @return a {@link CompletableFuture} that will complete once the SurrealDB server has
     * responded to the use operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> use(String namespace, String database) {
        return connection.rpc(executorService,"use", namespace, database);
    }

    /**
     * Sets a connection-wide parameter
     *
     * @param key   The parameter to set
     * @param value The value to set the parameter to
     * @return a {@link CompletableFuture} that will complete once the SurrealDB server has
     * responded to the set operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> setConnectionWideParameter(String key, Object value) {
        return connection.rpc(executorService,"let", key, value);
    }

    /**
     * Unset and clear a connection-wide parameter
     *
     * @param key The parameter to unset
     * @return a {@link CompletableFuture} that will complete once the SurrealDB server has
     * responded to the unset operation. If an error occurs, the future will complete exceptionally.
     */
    public CompletableFuture<Void> unsetConnectionWideParameter(String key) {
        return connection.rpc(executorService, "unset", key);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> query(String query, Map<String, Object> args, Class<? extends T> rowType) {
        Type queryResultType = TypeToken.getParameterized(QueryResult.class, rowType).getType();
        Type resultType = TypeToken.getParameterized(List.class, queryResultType).getType();
        CompletableFuture<List<QueryResult<T>>> future = connection.rpc(executorService, "query", resultType, query, args);

        return future.thenComposeAsync(this::checkResultsForErrors, executorService);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> query(String query, Class<? extends T> rowType) {
        return query(query, ImmutableMap.of(), rowType);
    }

    private <T> CompletableFuture<List<QueryResult<T>>> checkResultsForErrors(List<QueryResult<T>> queryResults) {
        for (QueryResult<T> queryResult : queryResults) {
            if (queryResult.getStatus().equals("ERR") && queryResult.getDetail() != null) {
                // Java 8 doesn't have CompletableFuture.failedFuture() so we have to do this...
                CompletableFuture<List<QueryResult<T>>> exceptionalFuture = new CompletableFuture<>();
                exceptionalFuture.completeExceptionally(SurrealExceptionUtils.createExceptionFromMessage(queryResult.getDetail()));
                return exceptionalFuture;
            }
        }

        return CompletableFuture.completedFuture(queryResults);
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
    public <T> CompletableFuture<Optional<T>> querySingle(String query, Map<String, Object> args, Class<? extends T> rowType) {
        CompletableFuture<List<QueryResult<T>>> rpcCallback = query(query, args, rowType);

        return rpcCallback.thenApplyAsync(queryResults -> {
            // If there are no query results, return an empty optional
            if (queryResults.isEmpty()) {
                return Optional.empty();
            }
            // Since there is at least one query, it's safe to get the first one
            QueryResult<T> firstQueryResult = queryResults.get(0);
            return getFirstElement(firstQueryResult.getResult());
        }, executorService);
    }

    public <T> CompletableFuture<List<T>> select(String thing, Class<? extends T> rowType) {
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return connection.rpc(executorService, "select", resultType, thing);
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
        return result.thenApplyAsync(this::getFirstElement, executorService);
    }

    public <T> CompletableFuture<T> create(String thing, T data) {
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        CompletableFuture<T> finalFuture = new CompletableFuture<>();

        CompletableFuture<List<T>> createFuture = connection.rpc(executorService, "create", resultType, thing, data);
        createFuture.whenCompleteAsync(((list, throwable) -> {
            if (throwable != null) {
                finalFuture.completeExceptionally(throwable);
            } else {
                finalFuture.complete(list.get(0));
            }
        }), executorService);

        return finalFuture;
    }

    public <T> CompletableFuture<List<T>> update(String thing, T data) {
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        return connection.rpc(executorService, "update", resultType, thing, data);
    }

    public <T, P> CompletableFuture<List<T>> change(String thing, P data, Class<T> rowType) {
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return connection.rpc(executorService, "change", resultType, thing, data);
    }

    public CompletableFuture<?> patch(String thing, List<Patch> patches) {
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return connection.rpc(executorService, "modify", resultType, thing, patches);
    }

    public CompletableFuture<?> delete(String thing) {
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return connection.rpc(executorService, "delete", resultType, thing);
    }

    @Override
    public SurrealConnection getSurrealConnection() {
        return connection;
    }

    @Override
    public ExecutorService getAsyncOperationExecutorService() {
        return executorService;
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
