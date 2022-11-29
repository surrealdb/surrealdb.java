package com.surrealdb;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.surrealdb.auth.SurrealAuthCredentials;
import com.surrealdb.patch.Patch;
import com.surrealdb.sql.QueryResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.surrealdb.InternalClientUtils.getResultSynchronously;

public sealed interface SurrealClient permits BiDirectionalSurrealClient, UniDirectionalSurrealClient {

    @NotNull CompletableFuture<Void> connectAsync(int timeout, @NotNull TimeUnit timeUnit);

    default void connect(int timeout, @NotNull TimeUnit timeUnit) {
        getResultSynchronously(connectAsync(timeout, timeUnit));
    }

    @NotNull CompletableFuture<Void> signInAsync(@NotNull SurrealAuthCredentials credentials);

    default void signIn(@NotNull SurrealAuthCredentials credentials) {
        getResultSynchronously(signInAsync(credentials));
    }

    @NotNull CompletableFuture<Void> signOutAsync();

    default void signOut() {
        getResultSynchronously(signOutAsync());
    }

    @NotNull CompletableFuture<Void> disconnectAsync();

    default void disconnect() {
        getResultSynchronously(disconnectAsync());
    }

    @NotNull CompletableFuture<Void> useAsync(@NotNull String namespace, @NotNull String database);

    default void use(@NotNull String namespace, @NotNull String database) {
        getResultSynchronously(useAsync(namespace, database));
    }

    @NotNull CompletableFuture<Void> pingAsync();

    default void ping() {
        getResultSynchronously(pingAsync());
    }

    @NotNull CompletableFuture<Void> setConnectionWideParameterAsync(@NotNull String key, @NotNull Object value);

    default void setConnectionWideParameter(@NotNull String key, @NotNull Object value) {
        getResultSynchronously(setConnectionWideParameterAsync(key, value));
    }

    @NotNull CompletableFuture<Void> unsetConnectionWideParameterAsync(@NotNull String key);

    default void unsetConnectionWideParameter(@NotNull String key) {
        getResultSynchronously(unsetConnectionWideParameterAsync(key));
    }

    @NotNull CompletableFuture<String> databaseVersionAsync();

    default @NotNull String databaseVersion() {
        return getResultSynchronously(databaseVersionAsync());
    }

    @NotNull CompletableFuture<Map<String, String>> infoAsync();

    default @NotNull Map<String, String> info() {
        return getResultSynchronously(infoAsync());
    }

    <T> @NotNull CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args);

    default <T> @NotNull CompletableFuture<Optional<T>> sqlSingleAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        CompletableFuture<List<QueryResult<T>>> queryFuture = sqlAsync(query, queryResult, args);
        ExecutorService executorService = getAsyncOperationExecutorService();
        return queryFuture.thenApplyAsync(InternalClientUtils::getFirstResultFromFirstQuery, executorService);
    }

    default <T> @NotNull CompletableFuture<List<T>> sqlFirstAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        CompletableFuture<List<QueryResult<T>>> queryFuture = sqlAsync(query, queryResult, args);
        ExecutorService executorService = getAsyncOperationExecutorService();
        return queryFuture.thenApplyAsync(InternalClientUtils::getResultsFromFirstQuery, executorService);
    }

    default <T> @NotNull CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlAsync(query, queryResult, ImmutableMap.of());
    }

    default <T> @NotNull List<QueryResult<T>> sql(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlAsync(query, queryResult, args));
    }

    default <T> @NotNull List<QueryResult<T>> sql(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlAsync(query, queryResult));
    }

    default <T> @NotNull Optional<T> sqlSingle(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlSingleAsync(query, queryResult, args));
    }

    default <T> @NotNull Optional<T> sqlSingle(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlSingleAsync(query, queryResult));
    }

    default <T> @NotNull List<T> sqlFirst(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlFirstAsync(query, queryResult, args));
    }

    default <T> @NotNull CompletableFuture<List<T>> sqlFirstAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlFirstAsync(query, queryResult, ImmutableMap.of());
    }

    default <T> @NotNull List<T> sqlFirst(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlFirstAsync(query, queryResult));
    }

    default <T> @NotNull CompletableFuture<Optional<T>> sqlSingleAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlSingleAsync(query, queryResult, ImmutableMap.of());
    }

    default <T> @NotNull CompletableFuture<List<T>> retrieveAllRecordsFromTableAsync(@NotNull SurrealTable<T> table) {
        // SQL query to retrieve all records from the table
        String sql = "SELECT * FROM type::table($tb);";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> query = sqlAsync(sql, table.getType(), args);
        // Return all records from the query
        ExecutorService executorService = getAsyncOperationExecutorService();
        return query.thenApplyAsync(InternalClientUtils::getResultsFromFirstQuery, executorService);
    }

    default <T> @NotNull List<T> retrieveAllRecordsFromTable(@NotNull SurrealTable<T> table) {
        return getResultSynchronously(retrieveAllRecordsFromTableAsync(table));
    }

    default <T> @NotNull CompletableFuture<Optional<T>> retrieveRecordAsync(@NotNull SurrealTable<T> table, @NotNull String record) {
        // SQL query to retrieve a record from the table
        String sql = "SELECT * FROM type::thing($what);";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record)
        );
        // Execute the query
        return sqlSingleAsync(sql, table.getType(), args);
    }

    default <T> @NotNull Optional<T> retrieveRecord(@NotNull SurrealTable<T> table, @NotNull String record) {
        return getResultSynchronously(retrieveRecordAsync(table, record));
    }

    default <T> @NotNull CompletableFuture<T> createRecordAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return createFuture.thenApplyAsync(Optional::get, executorService);
    }

    default <T> @NotNull T createRecord(@NotNull SurrealTable<T> table, @NotNull T data) {
        return getResultSynchronously(createRecordAsync(table, data));
    }

    default <T> @NotNull CompletableFuture<T> createRecordAsync(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull T data) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return createFuture.thenApplyAsync(Optional::get, executorService);
    }

    default <T> @NotNull T createRecord(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull T data) {
        return getResultSynchronously(createRecordAsync(table, record, data));
    }

    default <T> @NotNull CompletableFuture<List<T>> updateAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return updateFuture.thenApplyAsync(InternalClientUtils::getResultsFromFirstQuery, executorService);
    }

    default <T> @NotNull List<T> updateAllRecordsInTable(@NotNull SurrealTable<T> table, @NotNull T data) {
        return getResultSynchronously(updateAllRecordsInTableAsync(table, data));
    }

    default <T> @NotNull CompletableFuture<T> updateRecordAsync(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull T data) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return updateFuture.thenApplyAsync(Optional::get, executorService);
    }

    default <T> @NotNull T updateRecord(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull T data) {
        return getResultSynchronously(updateRecordAsync(table, record, data));
    }

    default <T> @NotNull CompletableFuture<List<T>> changeAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return changeFuture.thenApplyAsync(InternalClientUtils::getResultsFromFirstQuery, executorService);
    }

    default <T> @NotNull List<T> changeAllRecordsInTable(@NotNull SurrealTable<T> table, @NotNull T data) {
        return getResultSynchronously(changeAllRecordsInTableAsync(table, data));
    }

    default <T> @NotNull CompletableFuture<T> changeRecordAsync(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull T data) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return changeFuture.thenApplyAsync(Optional::get, executorService);
    }

    default <T> @NotNull T changeRecord(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull T data) {
        return getResultSynchronously(changeRecordAsync(table, record, data));
    }

    default <T> @NotNull CompletableFuture<List<T>> patchAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull List<Patch> patches) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return patchFuture.thenApplyAsync(InternalClientUtils::getResultsFromFirstQuery, executorService);
    }

    default <T> @NotNull List<T> patchAllRecordsInTable(@NotNull SurrealTable<T> table, @NotNull List<Patch> patches) {
        return getResultSynchronously(patchAllRecordsInTableAsync(table, patches));
    }

    default <T> @NotNull CompletableFuture<T> patchRecordAsync(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull List<Patch> patches) {
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
        ExecutorService executorService = getAsyncOperationExecutorService();
        return patchFuture.thenApplyAsync(Optional::get, executorService);
    }

    default <T> @NotNull T patchRecord(@NotNull SurrealTable<T> table, @NotNull String record, @NotNull List<Patch> patches) {
        return getResultSynchronously(patchRecordAsync(table, record, patches));
    }

    default <T> @NotNull CompletableFuture<@NotNull List<T>> deleteAllRecordsInTableAsync(@NotNull SurrealTable<T> table) {
        // SQL query to delete records
        String sql = "DELETE type::table($tb) RETURN BEFORE;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> deleteFuture = sqlAsync(sql, table.getType(), args);
        // Return the deleted records
        ExecutorService executorService = getAsyncOperationExecutorService();
        return deleteFuture.thenApplyAsync(InternalClientUtils::getResultsFromFirstQuery, executorService);
    }

    default <T> @NotNull List<T> deleteAllRecordsInTable(@NotNull SurrealTable<T> table) {
        return getResultSynchronously(deleteAllRecordsInTableAsync(table));
    }

    default <T> @NotNull CompletableFuture<T> deleteRecordAsync(@NotNull SurrealTable<T> table, @NotNull String record) {
        // SQL query to delete a record
        String sql = "DELETE type::thing($what) RETURN BEFORE;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(record)
        );
        // Execute the query
        CompletableFuture<Optional<T>> deleteFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the deleted record
        ExecutorService executorService = getAsyncOperationExecutorService();
        return deleteFuture.thenApplyAsync(Optional::get, executorService);
    }

    default <T> @NotNull T deleteRecord(@NotNull SurrealTable<T> table, @NotNull String record) {
        return getResultSynchronously(deleteRecordAsync(table, record));
    }

    @NotNull Gson getGson();

    /**
     * @return the {@link ExecutorService} this client is using for asynchronous operations.
     */
    @NotNull ExecutorService getAsyncOperationExecutorService();

}
