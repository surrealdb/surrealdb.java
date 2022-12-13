package com.surrealdb.client;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.auth.SurrealAuthCredentials;
import com.surrealdb.patch.Patch;
import com.surrealdb.query.QueryResult;
import com.surrealdb.types.Id;
import com.surrealdb.types.SurrealEdgeRecord;
import com.surrealdb.types.SurrealRecord;
import com.surrealdb.types.SurrealTable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.surrealdb.client.InternalClientUtils.getResultSynchronously;

/**
 * An interface representing a SurrealDB client, whether it be a uni or bidirectional client.
 */
public sealed interface SurrealClient permits SurrealBiDirectionalClient, SurrealUniDirectionalClient {

    /**
     * Cleans up the client, closing all connections and releasing all resources. It is recommended to call this method
     * when you are done using the client. Implementations may or may not be recoverable once this method is called.
     *
     * @return a {@link CompletableFuture} that will complete once the client has been cleaned up.
     */
    @NotNull CompletableFuture<Void> cleanupAsync();

    /**
     * Cleans up the client, closing all connections and releasing all resources. It is recommended to call this method
     * when you are done using the client. Implementations may or may not be recoverable once this method is called.
     * Blocks until the client has been cleaned up.
     */
    default void cleanup() {
        getResultSynchronously(cleanupAsync());
    }

    /**
     * Authenticates with the SurrealDB server using the given credentials.
     *
     * @param credentials the credentials to use for authentication
     * @return a {@link CompletableFuture} that will complete once the client has been authenticated.
     */
    @NotNull CompletableFuture<Void> signInAsync(@NotNull SurrealAuthCredentials credentials);

    /**
     * Authenticates with the SurrealDB server using the given credentials. Blocks until the client has been
     * authenticated.
     *
     * @param credentials the credentials to use for authentication
     */
    default void signIn(@NotNull SurrealAuthCredentials credentials) {
        getResultSynchronously(signInAsync(credentials));
    }

    /**
     * Signs out of the SurrealDB server.
     *
     * @return a {@link CompletableFuture} that will complete once the client has invalidated its session.
     */
    @NotNull CompletableFuture<Void> signOutAsync();

    /**
     * Signs out of the SurrealDB server. Blocks until the client has invalidated its session.
     */
    default void signOut() {
        getResultSynchronously(signOutAsync());
    }

    /**
     * Switches to the specified namespace and database.
     *
     * @param namespace the namespace to use
     * @param database  the database to use
     * @return a {@link CompletableFuture} that will complete once the client has switched to the given database.
     */
    @NotNull CompletableFuture<Void> setNamespaceAndDatabaseAsync(@NotNull String namespace, @NotNull String database);

    /**
     * Switches to the specified namespace and database. Blocks until the client has switched to the given database.
     *
     * @param namespace the namespace to use
     * @param database  the database to use
     */
    default void setNamespaceAndDatabase(@NotNull String namespace, @NotNull String database) {
        getResultSynchronously(setNamespaceAndDatabaseAsync(namespace, database));
    }

    /**
     * Pings the SurrealDB server.
     *
     * @return a {@link CompletableFuture} that will complete once the server has responded to the ping request.
     */
    @NotNull CompletableFuture<Void> pingAsync();

    /**
     * Pings the SurrealDB server. Blocks until the server has responded to the ping request.
     */
    default void ping() {
        getResultSynchronously(pingAsync());
    }

    /**
     * Sets a connection wide variable. Use '{@code $key}' in future queries to access the value.
     *
     * @param key   the key to set
     * @param value the value to set
     * @return a {@link CompletableFuture} that will complete once the key has been set.
     */
    @NotNull CompletableFuture<Void> setConnectionWideParameterAsync(@NotNull String key, @NotNull Object value);

    /**
     * Sets a connection wide variable. Use '{@code $key}' in future queries to access the value. Blocks until the key
     * has been set.
     *
     * @param key   the key to set
     * @param value the value to set
     */
    default void setConnectionWideParameter(@NotNull String key, @NotNull Object value) {
        getResultSynchronously(setConnectionWideParameterAsync(key, value));
    }

    /**
     * Removes a connection wide variable.
     *
     * @param key the parameter to remove
     * @return a {@link CompletableFuture} that will complete once the parameter has been removed.
     */
    @NotNull CompletableFuture<Void> unsetConnectionWideParameterAsync(@NotNull String key);

    /**
     * Removes a connection wide variable. Blocks until the parameter has been removed.
     *
     * @param key the parameter to remove
     */
    default void unsetConnectionWideParameter(@NotNull String key) {
        getResultSynchronously(unsetConnectionWideParameterAsync(key));
    }

    /**
     * Asks the SurrealDB server what version it is running.
     *
     * @return a {@link CompletableFuture} that will complete with the SurrealDB server version.
     */
    @NotNull CompletableFuture<String> databaseVersionAsync();

    /**
     * Asks the SurrealDB server what version it is running. Blocks until the SurrealDB server version is returned.
     *
     * @return the SurrealDB server version.
     */
    default @NotNull String databaseVersion() {
        return getResultSynchronously(databaseVersionAsync());
    }

    /**
     * Executes one or more queries against the SurrealDB server.
     *
     * @param query      the query to execute
     * @param resultType the type of the result
     * @param args       a map of arguments to use in the query. Use '{@code $key}' in the query to access the value.
     * @param <T>        the type of the result
     * @return a {@link CompletableFuture} that will complete with the result of the query.
     */
    <T> @NotNull CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> resultType, @NotNull Map<String, Object> args);

    /**
     * Executes one or more queries against the SurrealDB server. Blocks until the queries have been executed.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param args        a map of arguments to use in the query. Use '{@code $key}' in the query to access the value.
     * @param <T>         the type of the result
     * @return a list containing all query results
     */
    default <T> @NotNull List<QueryResult<T>> sql(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlAsync(query, queryResult, args));
    }

    /**
     * Executes a single query against the SurrealDB server. Returns a list containing the results of the first query.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param <T>         the type of the result
     * @return a {@link CompletableFuture} that will complete with the result of the first query.
     */
    default <T> @NotNull CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlAsync(query, queryResult, ImmutableMap.of());
    }

    /**
     * Executes one or more queries against the SurrealDB server. Blocks until the queries have been executed.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param <T>         the type of the result
     * @return a list containing all query results
     */
    default <T> @NotNull List<QueryResult<T>> sql(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlAsync(query, queryResult));
    }

    /**
     * Executes a query against the SurrealDB server. Returns an optional containing the first result of the first query.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param args        a map of arguments to use in the query. Use '{@code $key}' in the query to access the value.
     * @param <T>         the type of the result
     * @return a {@link CompletableFuture} that will complete with an optional containing the first result of the first query, or an empty optional if the query returned no results.
     */
    default <T> @NotNull CompletableFuture<Optional<T>> sqlSingleAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        CompletableFuture<List<QueryResult<T>>> queryFuture = sqlAsync(query, queryResult, args);
        ExecutorService executorService = getAsyncOperationExecutorService();
        return queryFuture.thenApplyAsync(InternalClientUtils::getFirstResultFromFirstQuery, executorService);
    }

    /**
     * Executes a query against the SurrealDB server. Returns an optional containing the first result of the first query.
     * Blocks until the query has been executed.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param args        a map of arguments to use in the query. Use '{@code $key}' in the query to access the value.
     * @param <T>         the type of the result
     * @return an optional containing the first result of the first query, or an empty optional if the query returned no results.
     */
    default <T> @NotNull Optional<T> sqlSingle(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlSingleAsync(query, queryResult, args));
    }

    /**
     * Executes a query against the SurrealDB server. Returns an optional containing the first result of the first query.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param <T>         the type of the result
     * @return a {@link CompletableFuture} that will complete with the result of the first query.
     */
    default <T> @NotNull CompletableFuture<Optional<T>> sqlSingleAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlSingleAsync(query, queryResult, ImmutableMap.of());
    }

    /**
     * Executes a query against the SurrealDB server. Returns an optional containing the first result of the first query.
     * Blocks until the query has been executed.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param <T>         the type of the result
     * @return an optional containing the first result of the first query, or an empty optional if the query returned no results.
     */
    default <T> @NotNull Optional<T> sqlSingle(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlSingleAsync(query, queryResult));
    }

    /**
     * Executes a single query against the SurrealDB server. Returns a list containing the results of the first query.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param args        a map of arguments to use in the query. Use '{@code $key}' in the query to access the value.
     * @param <T>         the type of the result
     * @return a {@link CompletableFuture} that will complete with the result of the first query.
     */
    default <T> @NotNull CompletableFuture<List<T>> sqlFirstAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        CompletableFuture<List<QueryResult<T>>> queryFuture = sqlAsync(query, queryResult, args);
        ExecutorService executorService = getAsyncOperationExecutorService();
        return queryFuture.thenApplyAsync(InternalClientUtils::getResultsFromFirstQuery, executorService);
    }

    /**
     * Executes a single query against the SurrealDB server. Returns a list containing the results of the first query.
     * Blocks until the query has been executed.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param args        a map of arguments to use in the query. Use '{@code $key}' in the query to access the value.
     * @param <T>         the type of the result
     * @return a list containing the results of the first query.
     */
    default <T> @NotNull List<T> sqlFirst(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlFirstAsync(query, queryResult, args));
    }

    /**
     * Executes a single query against the SurrealDB server. Returns a list containing the results of the first query.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param <T>         the type of the result
     * @return a {@link CompletableFuture} that will complete with the result of the first query.
     */
    default <T> @NotNull CompletableFuture<List<T>> sqlFirstAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlFirstAsync(query, queryResult, ImmutableMap.of());
    }

    /**
     * Executes a single query against the SurrealDB server. Returns a list containing the results of the first query.
     *
     * @param query       the query to execute
     * @param queryResult the type of the result
     * @param <T>         the type of the result
     * @return a list containing the results of the first query.
     */
    default <T> @NotNull List<T> sqlFirst(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlFirstAsync(query, queryResult));
    }

    /**
     * Retrieves all records from the specified table.
     *
     * @param table the table to retrieve all records from
     * @param <T>   the type of the records
     * @return a {@link CompletableFuture} that will complete with a list of all records in the table.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<List<T>> retrieveAllRecordsFromTableAsync(@NotNull SurrealTable<T> table) {
        // SQL query to retrieve all records from the table
        String sql = "SELECT * FROM type::table($tb);";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        return sqlFirstAsync(sql, table.getType(), args);
    }

    /**
     * Retrieves all records from the specified table. Blocks until all records have been retrieved.
     *
     * @param table the table to retrieve all records from
     * @param <T>   the type of the records
     * @return a list of all records in the table.
     */
    default <T extends SurrealRecord> @NotNull List<T> retrieveAllRecordsFromTable(@NotNull SurrealTable<T> table) {
        return getResultSynchronously(retrieveAllRecordsFromTableAsync(table));
    }

    /**
     * Retrieves the specified record from the specified table.
     *
     * @param table    the table to retrieve the record from
     * @param recordId the record to retrieve
     * @param <T>      the type of the record
     * @return a {@link CompletableFuture} that will complete with the record if it exists, or an empty {@link Optional} if it does not.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<Optional<T>> retrieveRecordAsync(@NotNull SurrealTable<T> table, @NotNull String recordId) {
        // SQL query to retrieve a record from the table
        String sql = "SELECT * FROM type::thing($what);";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(recordId)
        );
        // Execute the query
        return sqlSingleAsync(sql, table.getType(), args);
    }

    /**
     * Retrieves the specified record from the specified table. Blocks until the record has been retrieved.
     *
     * @param table    the table to retrieve the record from
     * @param recordId the record to retrieve
     * @param <T>      the type of the record
     * @return the record if it exists, or an empty {@link Optional} if it does not.
     */
    default <T extends SurrealRecord> @NotNull Optional<T> retrieveRecord(@NotNull SurrealTable<T> table, @NotNull String recordId) {
        return getResultSynchronously(retrieveRecordAsync(table, recordId));
    }

    /**
     * Creates a new record with a SurrealDB assigned id in the specified table.
     *
     * @param table the table to create the record in
     * @param data  the record to create
     * @param <T>   the type of the record
     * @return a {@link CompletableFuture} that will complete with the created record.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<T> createRecordAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
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

    /**
     * Creates a new record with a SurrealDB assigned id in the specified table. Blocks until the record has been created.
     *
     * @param table the table to create the record in
     * @param data  the record to create
     * @param <T>   the type of the record
     * @return the created record.
     */
    default <T extends SurrealRecord> @NotNull T createRecord(@NotNull SurrealTable<T> table, @NotNull T data) {
        return getResultSynchronously(createRecordAsync(table, data));
    }

    /**
     * Creates a new record with the specified id in the specified table.
     *
     * @param table    the table to create the record in
     * @param recordId the id of the record to create
     * @param data     the record to create
     * @param <T>      the type of the record
     * @return a {@link CompletableFuture} that will complete with the created record.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<T> createRecordAsync(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull T data) {
        // SQL query to create a record
        String sql = "CREATE type::thing($what) CONTENT $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(recordId),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> createFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the created record
        ExecutorService executorService = getAsyncOperationExecutorService();
        return createFuture.thenApplyAsync(Optional::get, executorService);
    }

    /**
     * Creates a new record with the specified id in the specified table. Blocks until the record has been created.
     *
     * @param table    the table to create the record in
     * @param recordId the record to create
     * @param data     the record to create
     * @param <T>      the type of the record
     * @return the created record.
     */
    default <T extends SurrealRecord> @NotNull T createRecord(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull T data) {
        return getResultSynchronously(createRecordAsync(table, recordId, data));
    }

    /**
     * Sets all records in the specified table to the given data.
     *
     * @param table the table in which to set the records
     * @param data  the data to set the records to
     * @param <T>   the type of the records
     * @return a {@link CompletableFuture} that will complete with a list of all updated records.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<List<T>> setAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull T data) {
        // SQL query to update records
        String sql = "UPDATE type::table($tb) CONTENT $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        return sqlFirstAsync(sql, table.getType(), args);
    }

    /**
     * Sets all records in the specified table to the given data. Blocks until the records have been updated.
     *
     * @param table the table in which to set the records
     * @param data  the data to set the records to
     * @param <T>   the type of the records
     * @return a list of all updated records.
     */
    default <T extends SurrealRecord> @NotNull List<T> setAllRecordsInTable(@NotNull SurrealTable<T> table, @NotNull T data) {
        return getResultSynchronously(setAllRecordsInTableAsync(table, data));
    }

    /**
     * Sets the specified record to the given data.
     *
     * @param table    the table in which to set the record
     * @param recordId the record to set
     * @param data     the data to update the record with
     * @param <T>      the type of the record
     * @return a {@link CompletableFuture} that will complete with the updated record.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<T> setRecordAsync(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull T data) {
        // SQL query to update a record
        String sql = "UPDATE type::thing($what) CONTENT $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(recordId),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> updateFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the updated record
        ExecutorService executorService = getAsyncOperationExecutorService();
        return updateFuture.thenApplyAsync(Optional::get, executorService);
    }

    /**
     * Sets the specified record to the given data. Blocks until the record has been updated.
     *
     * @param table    the table in which to set the record
     * @param recordId the record to set
     * @param data     the data to update the record with
     * @param <T>      the type of the record
     * @return the updated record.
     */
    default <T extends SurrealRecord> @NotNull T setRecord(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull T data) {
        return getResultSynchronously(setRecordAsync(table, recordId, data));
    }

    /**
     * Changes all records in the specified table.
     *
     * @param table the table to change records in
     * @param data  the data to change the records with
     * @param <T>   the type of the records
     * @return a {@link CompletableFuture} that will complete with a list of all changed records.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<List<T>> changeAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull Object data) {
        // SQL query to change records
        String sql = "UPDATE type::table($tb) MERGE $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName(),
            "data", data
        );
        // Execute the query
        return sqlFirstAsync(sql, table.getType(), args);
    }

    /**
     * Changes all records in the specified table. Blocks until the records have been changed.
     *
     * @param table the table to change records in
     * @param data  the data to change the records with
     * @param <T>   the type of the records
     * @return a list of all changed records.
     */
    default <T extends SurrealRecord> @NotNull List<T> changeAllRecordsInTable(@NotNull SurrealTable<T> table, @NotNull Object data) {
        return getResultSynchronously(changeAllRecordsInTableAsync(table, data));
    }

    /**
     * Changes the specified record in the specified table.
     *
     * @param table    the table of the record to change
     * @param recordId the record to change
     * @param data     the data to change the record with
     * @param <T>      the type of the record
     * @return a {@link CompletableFuture} that will complete with the changed record.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<T> changeRecordAsync(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull Object data) {
        // SQL query to change a record
        String sql = "UPDATE type::thing($what) MERGE $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(recordId),
            "data", data
        );
        // Execute the query
        CompletableFuture<Optional<T>> changeFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the changed record
        ExecutorService executorService = getAsyncOperationExecutorService();
        return changeFuture.thenApplyAsync(Optional::get, executorService);
    }

    /**
     * Changes a record in the specified table. Blocks until the record has been changed.
     *
     * @param table    the table of the record to change
     * @param recordId the record to change
     * @param data     the data to change the record with
     * @param <T>      the type of the record
     * @return the changed record.
     */
    default <T extends SurrealRecord> @NotNull T changeRecord(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull Object data) {
        return getResultSynchronously(changeRecordAsync(table, recordId, data));
    }

    /**
     * Patches all records in the specified table.
     *
     * @param table   the table to patch records in
     * @param patches the patches to apply to the records
     * @param <T>     the type of the records
     * @return a {@link CompletableFuture} that will complete with a list of all patched records.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<List<T>> patchAllRecordsInTableAsync(@NotNull SurrealTable<T> table, @NotNull List<Patch> patches) {
        // SQL query to patch an entire table
        String sql = "UPDATE type::table($tb) PATCH $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName(),
            "data", patches
        );
        // Execute the query
        return sqlFirstAsync(sql, table.getType(), args);
    }

    /**
     * Patches all records in the specified table. Blocks until the records have been patched.
     *
     * @param table   the table to patch records in
     * @param patches the patches to apply to the records
     * @param <T>     the type of the records
     * @return a list of all patched records.
     */
    default <T extends SurrealRecord> @NotNull List<T> patchAllRecordsInTable(@NotNull SurrealTable<T> table, @NotNull List<Patch> patches) {
        return getResultSynchronously(patchAllRecordsInTableAsync(table, patches));
    }

    /**
     * Patches the specified record in the specified table.
     *
     * @param table    the table of the record to patch
     * @param recordId the record to patch
     * @param patches  the patches to apply to the record
     * @param <T>      the type of the record
     * @return a {@link CompletableFuture} that will complete with the patched record.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<T> patchRecordAsync(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull List<Patch> patches) {
        // SQL query to patch a record
        String sql = "UPDATE type::thing($what) PATCH $data RETURN AFTER;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(recordId),
            "data", patches
        );
        // Execute the query
        CompletableFuture<Optional<T>> patchFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the patched record
        ExecutorService executorService = getAsyncOperationExecutorService();
        return patchFuture.thenApplyAsync(Optional::get, executorService);
    }

    /**
     * Patches a record in the specified table. Blocks until the record has been patched.
     *
     * @param table    the table of the record to patch
     * @param recordId the record to patch
     * @param patches  the patches to apply to the record
     * @param <T>      the type of the record
     * @return the patched record.
     */
    default <T extends SurrealRecord> @NotNull T patchRecord(@NotNull SurrealTable<T> table, @NotNull String recordId, @NotNull List<Patch> patches) {
        return getResultSynchronously(patchRecordAsync(table, recordId, patches));
    }

    /**
     * Deletes all records in the specified table.
     *
     * @param table the table to delete records from
     * @param <T>   the type of the records
     * @return a {@link CompletableFuture} that will complete with a list of all deleted records.
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<@NotNull List<T>> deleteAllRecordsInTableAsync(@NotNull SurrealTable<T> table) {
        // SQL query to delete records
        String sql = "DELETE type::table($tb) RETURN BEFORE;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        return sqlFirstAsync(sql, table.getType(), args);
    }

    /**
     * Deletes all records in the specified table. Blocks until the records have been deleted.
     *
     * @param table the table to delete records from
     * @param <T>   the type of the records
     * @return a list of all deleted records.
     */
    default <T extends SurrealRecord> @NotNull List<T> deleteAllRecordsInTable(@NotNull SurrealTable<T> table) {
        return getResultSynchronously(deleteAllRecordsInTableAsync(table));
    }

    /**
     * Deletes a record from the specified table.
     *
     * @param table    The table of the record to be deleted
     * @param recordId The record to delete
     * @param <T>      The type of the record
     * @return The deleted record
     */
    default <T extends SurrealRecord> @NotNull CompletableFuture<T> deleteRecordAsync(@NotNull SurrealTable<T> table, @NotNull String recordId) {
        // SQL query to delete a record
        String sql = "DELETE type::thing($what) RETURN BEFORE;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "what", table.makeThing(recordId)
        );
        // Execute the query
        CompletableFuture<Optional<T>> deleteFuture = sqlSingleAsync(sql, table.getType(), args);
        // Return the deleted record
        ExecutorService executorService = getAsyncOperationExecutorService();
        return deleteFuture.thenApplyAsync(Optional::get, executorService);
    }

    /**
     * Delete a record from the specified table. Blocks until the record has been deleted.
     *
     * @param table  The table to delete the record from
     * @param record The record to delete
     * @param <T>    The type of the record
     * @return The deleted record
     */
    default <T extends SurrealRecord> @NotNull T deleteRecord(@NotNull SurrealTable<T> table, @NotNull String record) {
        return getResultSynchronously(deleteRecordAsync(table, record));
    }

    default <T extends SurrealRecord> @NotNull CompletableFuture<T> relateAsync(@NotNull Id from, @NotNull SurrealTable<T> edgeTable, @NotNull Id with, @NotNull T data) {
        // SQL query to relate two records
        // I would like to use parameters, but putting the values inline seems to be the only thing that works...
        String sql = String.format(
            "RELATE %s->%s->%s CONTENT $data RETURN AFTER;",
            from.toCombinedId(),
            edgeTable.getName(),
            with.toCombinedId()
        );
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of("data", data);
        // Execute the query
        CompletableFuture<Optional<T>> relateFuture = sqlSingleAsync(sql, edgeTable.getType(), args);
        // Return the related record
        ExecutorService executorService = getAsyncOperationExecutorService();
        return relateFuture.thenApplyAsync(Optional::get, executorService);
    }

    default <T extends SurrealEdgeRecord> @NotNull T relate(@NotNull Id from, @NotNull SurrealTable<T> edgeTable, @NotNull Id to, @NotNull T data) {
        return getResultSynchronously(relateAsync(from, edgeTable, to, data));
    }

    default @NotNull CompletableFuture<Long> getNumberOfRecordsInTableAsync(@NotNull SurrealTable<?> table) {
        // SQL query to count records
        String sql = "SELECT count() AS recordCount FROM type::table($tb) GROUP BY ALL;";
        // Arguments to use in the query
        Map<String, Object> args = ImmutableMap.of(
            "tb", table.getName()
        );
        // Execute the query
        ExecutorService asyncOperationExecutorService = getAsyncOperationExecutorService();
        CompletableFuture<Optional<JsonObject>> countFuture = sqlSingleAsync(sql, JsonObject.class, args);
        return countFuture.thenApplyAsync(optional -> {
            if (optional.isEmpty()) {
                return 0L;
            }

            JsonObject jsonObject = optional.get();
            JsonElement recordCount = jsonObject.get("recordCount");
            return recordCount.getAsLong();
        }, asyncOperationExecutorService);
    }

    default long getNumberOfRecordsInTable(@NotNull SurrealTable<?> table) {
        return getResultSynchronously(getNumberOfRecordsInTableAsync(table));
    }

    /**
     * @return The {@link Gson} instance used to serialize and deserialize data.
     */
    @NotNull Gson getGson();

    /**
     * @return the {@link ExecutorService} this client is using for asynchronous operations.
     */
    @NotNull ExecutorService getAsyncOperationExecutorService();

}
