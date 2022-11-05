package com.surrealdb.driver;

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealExceptionUtils;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.patch.Patch;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class SurrealDriverImpl implements SurrealDriver {

    private final SurrealConnection connection;
    private final ExecutorService executorService;

    public SurrealDriverImpl(SurrealConnection connection, SurrealDriverSettings settings) {
        this.connection = connection;
        this.executorService = settings.getAsyncOperationExecutorService();
    }

    public SurrealDriverImpl(SurrealConnection connection) {
        this(connection, SurrealDriverSettings.DEFAULT);
    }

    public CompletableFuture<Void> pingAsync() {
        return connection.rpc(executorService, "ping");
    }

    public CompletableFuture<String> getDatabaseVersionAsync() {
        return connection.rpc(executorService, "version", String.class);
    }

    public CompletableFuture<Map<String, String>> infoAsync() {
        Type resultType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();
        return connection.rpc(executorService, "info", resultType);
    }

    public CompletableFuture<Void> signInAsync(SurrealAuthCredentials credentials) {
        return connection.rpc(executorService, "signin", credentials);
    }

    public CompletableFuture<Void> useAsync(String namespace, String database) {
        return connection.rpc(executorService, "use", namespace, database);
    }

    public CompletableFuture<Void> setConnectionWideParameterAsync(String key, Object value) {
        return connection.rpc(executorService, "let", key, value);
    }

    public CompletableFuture<Void> unsetConnectionWideParameterAsync(String key) {
        return connection.rpc(executorService, "unset", key);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> queryAsync(String query, Class<T> queryResult, Map<String, Object> params) {
        // QueryResult<T>
        TypeToken<?> queryType = TypeToken.getParameterized(QueryResult.class, queryResult);
        // List<QueryResult<T>>
        Type resultType = TypeToken.getParameterized(List.class, queryType.getType()).getType();
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> queryFuture = connection.rpc(executorService, "query", resultType, query, params);
        // Check for errors and return the result
        return queryFuture.thenComposeAsync(this::checkResultsForErrors, executorService);
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

    public <T> CompletableFuture<Optional<T>> querySingleAsync(String query, Class<T> queryResult, Map<String, Object> parameters) {
        CompletableFuture<List<QueryResult<T>>> queryFuture = queryAsync(query, queryResult, parameters);
        return queryFuture.thenApplyAsync(this::getFirstResultFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<List<T>> retrieveAllRecordsFromTableAsync(SurrealTable<T> table) {
        // SQL query to retrieve all records from the table
        String sql = "SELECT * FROM type::table($tb);";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> query = queryAsync(sql, table.getType(), parameters);
        // Return all records from the query
        return query.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<Optional<T>> retrieveRecordAsync(SurrealTable<T> table, String record) {
        // SQL query to retrieve a record from the table
        String sql = "SELECT * FROM type::thing($what);";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "what", table.makeThing(record)
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> query = queryAsync(sql, table.getType(), parameters);
        // Return the first record from the query
        return query.thenApplyAsync(this::getFirstResultFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<T> createRecordAsync(SurrealTable<T> table, String record, T data) {
        // SQL query to create a record
        String sql = "CREATE type::thing($what) CONTENT $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> createFuture = querySingleAsync(sql, table.getType(), parameters);
        // Return the created record
        return createFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<T> createRecordAsync(SurrealTable<T> table, T data) {
        // SQL query to create a record
        String sql = "CREATE type::table($tb) CONTENT $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> createFuture = querySingleAsync(sql, table.getType(), parameters);
        // Return the created record
        return createFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<T> updateRecordAsync(SurrealTable<T> table, String record, T data) {
        // SQL query to update a record
        String sql = "UPDATE type::thing($what) CONTENT $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> updateFuture = querySingleAsync(sql, table.getType(), parameters);
        // Return the updated record
        return updateFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<List<T>> updateAllRecordsInTableAsync(SurrealTable<T> table, T data) {
        // SQL query to update records
        String sql = "UPDATE type::table($tb) CONTENT $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> updateFuture = queryAsync(sql, table.getType(), parameters);
        // Return the updated records
        return updateFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<List<T>> changeAllRecordsInTableAsync(SurrealTable<T> table, T data) {
        // SQL query to change records
        String sql = "UPDATE type::table($tb) MERGE $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> changeFuture = queryAsync(sql, table.getType(), parameters);
        // Return the changed records
        return changeFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<T> changeRecordAsync(SurrealTable<T> table, String record, T data) {
        // SQL query to change a record
        String sql = "UPDATE type::thing($what) MERGE $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> changeFuture = querySingleAsync(sql, table.getType(), parameters);
        // Return the changed record
        return changeFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<List<T>> patchAllRecordsInTableAsync(SurrealTable<T> table, List<Patch> patches) {
        // SQL query to patch an entire table
        String sql = "UPDATE type::table($tb) PATCH $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "tb", table.getName(),
            "data", patches
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> patchFuture = queryAsync(sql, table.getType(), parameters);
        // Return the patched records
        return patchFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<T> patchRecordAsync(SurrealTable<T> table, String record, List<Patch> patches) {
        // SQL query to patch a record
        String sql = "UPDATE type::thing($what) PATCH $data RETURN AFTER;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "what", table.makeThing(record),
            "data", patches
        );
        // Execute the query
        CompletableFuture<Optional<T>> patchFuture = querySingleAsync(sql, table.getType(), parameters);
        // Return the patched record
        return patchFuture.thenApplyAsync(Optional::get, executorService);
    }

    public <T> CompletableFuture<List<T>> deleteAllRecordsInTableAsync(SurrealTable<T> table) {
        // SQL query to delete records
        String sql = "DELETE type::table($tb) RETURN BEFORE;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> deleteFuture = queryAsync(sql, table.getType(), parameters);
        // Return the deleted records
        return deleteFuture.thenApplyAsync(this::getResultsFromFirstQuery, executorService);
    }

    public <T> CompletableFuture<T> deleteRecordAsync(SurrealTable<T> table, String record) {
        // SQL query to delete a record
        String sql = "DELETE type::thing($what) RETURN BEFORE;";
        // Parameters to use in the query
        Map<String, Object> parameters = ImmutableMap.of(
            "what", table.makeThing(record)
        );
        // Execute the query
        CompletableFuture<Optional<T>> deleteFuture = querySingleAsync(sql, table.getType(), parameters);
        // Return the deleted record
        return deleteFuture.thenApplyAsync(Optional::get, executorService);
    }

    @Override
    public SurrealConnection getSurrealConnection() {
        return connection;
    }

    @Override
    public ExecutorService getAsyncOperationExecutorService() {
        return executorService;
    }

    private <T> Optional<T> getFirstResultFromFirstQuery(List<QueryResult<T>> queryResults) {
        List<T> resultsFromFirstQuery = getResultsFromFirstQuery(queryResults);
        return getFirstElement(resultsFromFirstQuery);
    }

    private <T> List<T> getResultsFromFirstQuery(List<QueryResult<T>> queryResults) {
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
    private <T> Optional<T> getFirstElement(List<T> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
