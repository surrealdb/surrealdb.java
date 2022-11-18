package com.surrealdb.driver;

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealExceptionUtils;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.patch.Patch;
import com.surrealdb.driver.sql.QueryResult;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class StandardSurrealDriver implements SurrealDriver {

    @NotNull SurrealConnection connection;
    @NotNull ExecutorService executorService;

    public StandardSurrealDriver(@NotNull SurrealConnection connection, @NotNull SurrealDriverSettings settings) {
        this.connection = connection;
        this.executorService = settings.getAsyncOperationExecutorService();
    }

    public @NotNull CompletableFuture<Void> pingAsync() {
        return connection.rpc(executorService, "ping");
    }

    public @NotNull CompletableFuture<String> databaseVersionAsync() {
        return connection.rpc(executorService, "version", String.class);
    }

    public @NotNull CompletableFuture<Map<String, String>> infoAsync() {
        Type resultType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();
        return connection.rpc(executorService, "info", resultType);
    }

    public @NotNull CompletableFuture<Void> signInAsync(@NotNull SurrealAuthCredentials credentials) {
        return connection.rpc(executorService, "signin", credentials);
    }

    public @NotNull CompletableFuture<Void> useAsync(@NotNull String namespace, @NotNull String database) {
        return connection.rpc(executorService, "use", namespace, database);
    }

    public @NotNull CompletableFuture<Void> setConnectionWideParameterAsync(@NotNull String key, @NotNull Object value) {
        return connection.rpc(executorService, "let", key, value);
    }

    public @NotNull CompletableFuture<Void> unsetConnectionWideParameterAsync(@NotNull String key) {
        return connection.rpc(executorService, "unset", key);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        // QueryResult<T>
        TypeToken<?> queryType = TypeToken.getParameterized(QueryResult.class, queryResult);
        // List<QueryResult<T>>
        Type resultType = TypeToken.getParameterized(List.class, queryType.getType()).getType();
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> queryFuture = connection.rpc(executorService, "query", resultType, query, args);
        // Check for errors and return the result
        return queryFuture.thenComposeAsync(this::checkResultsForErrors, executorService);
    }

    private <T> @NotNull CompletableFuture<List<QueryResult<T>>> checkResultsForErrors(@NotNull List<QueryResult<T>> queryResults) {
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

    public <T> CompletableFuture<Optional<T>> sqlSingleAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        CompletableFuture<List<QueryResult<T>>> queryFuture = sqlAsync(query, queryResult, args);
        return queryFuture.thenApplyAsync(this::getFirstResultFromFirstQuery, executorService);
    }

    @Override
    public <T> CompletableFuture<List<T>> sqlFirstAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        CompletableFuture<List<QueryResult<T>>> queryFuture = sqlAsync(query, queryResult, args);
        return queryFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<List<T>> retrieveAllRecordsFromTableAsync(@NotNull SurrealTable<T> table) {
        // SQL query to retrieve all records from the table
        String sql = "SELECT * FROM type::table($tb);";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> query = sqlAsync(sql, table.getType(), args);
        // Return all records from the query
        return query.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<Optional<T>> retrieveRecordAsync(@NotNull SurrealTable<T> table, String record) {
        // SQL query to retrieve a record from the table
        String sql = "SELECT * FROM type::thing($what);";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record)
        );
        // Execute the query
        return sqlSingleAsync(sql, table.getType(), args);
    }

    public <T> CompletableFuture<T> createRecordAsync(@NotNull SurrealTable<T> table, String record, @NotNull T data) {
        // SQL query to create a record
        String sql = "CREATE type::thing($what) CONTENT $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> createFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the created record
        return createFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<T> createRecordAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
        // SQL query to create a record
        String sql = "CREATE type::table($tb) CONTENT $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> createFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the created record
        return createFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<T> updateRecordAsync(@NotNull SurrealTable<T> table, String record, @NotNull T data) {
        // SQL query to update a record
        String sql = "UPDATE type::thing($what) CONTENT $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> updateFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the updated record
        return updateFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<List<T>> updateAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
        // SQL query to update records
        String sql = "UPDATE type::table($tb) CONTENT $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> updateFuture = sqlAsync(sql, table.getType(), args);
        // Return the updated records
        return updateFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<List<T>> changeAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
        // SQL query to change records
        String sql = "UPDATE type::table($tb) MERGE $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> changeFuture = sqlAsync(sql, table.getType(), args);
        // Return the changed records
        return changeFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<T> changeRecordAsync(@NotNull SurrealTable<T> table, String record, @NotNull T data) {
        // SQL query to change a record
        String sql = "UPDATE type::thing($what) MERGE $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> changeFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the changed record
        return changeFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<List<T>> patchAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull List<Patch> patches) {
        // SQL query to patch an entire table
        String sql = "UPDATE type::table($tb) PATCH $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName(),
            "data", patches
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> patchFuture = sqlAsync(sql, table.getType(), args);
        // Return the patched records
        return patchFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<T> patchRecordAsync(@NotNull SurrealTable<T> table, String record, @NotNull List<Patch> patches) {
        // SQL query to patch a record
        String sql = "UPDATE type::thing($what) PATCH $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", patches
        );
        // Execute the query
        CompletableFuture<Optional<T>> patchFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the patched record
        return patchFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<List<T>> deleteAllRecordsInTableAsync(@NotNull SurrealTable<T> table) {
        // SQL query to delete records
        String sql = "DELETE type::table($tb) RETURN BEFORE;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> deleteFuture = sqlAsync(sql, table.getType(), args);
        // Return the deleted records
        return deleteFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<T> deleteRecordAsync(@NotNull SurrealTable<T> table, String record) {
        // SQL query to delete a record
        String sql = "DELETE type::thing($what) RETURN BEFORE;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record)
        );
        // Execute the query
        CompletableFuture<Optional<T>> deleteFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the deleted record
        return deleteFuture.thenApplyAsync(Optional::get, executorService);
    }

    @Override
    public @NotNull SurrealConnection getSurrealConnection() {
        return connection;
    }

    @Override
    public @NotNull ExecutorService getAsyncOperationExecutorService() {
        return executorService;
    }

    private <T> @NotNull Optional<T> getFirstResultFromFirstQuery(@NotNull List<QueryResult<T>> queryResults) {
        List<T> resultsFromFirstQuery = getResultsFromFirstQuery(queryResults);
        return getFirstElement(resultsFromFirstQuery);
    }

    private <T> @NotNull List<T> getResultsFromFirstQuery(@NotNull List<QueryResult<T>> queryResults) {
        // If there are no query results, return an empty list
        if (queryResults.isEmpty()) {
            return Collections.emptyList();
        }
        // Since there is at least one query, it's safe to get the first one
        QueryResult<T> firstQueryResult = queryResults.get(0);
        return firstQueryResult.getResult();
    }

    /**
     * @param list The list to get the first element from
     * @param <T>  The type of the list
     * @return the first element in the list or an empty {@link Optional} if the list is empty
     */
    private <T> @NotNull Optional<T> getFirstElement(@NotNull List<T> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
