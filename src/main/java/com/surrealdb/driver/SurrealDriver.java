package com.surrealdb.driver;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.patch.Patch;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.surrealdb.driver.AsyncUtils.getResultSynchronously;

public interface SurrealDriver {

    static @NotNull SurrealDriver create(@NotNull SurrealConnection connection, @NotNull SurrealDriverSettings settings) {
        return new SurrealDriverImpl(connection, settings);
    }

    static @NotNull SurrealDriver create(@NotNull SurrealConnection connection) {
        return new SurrealDriverImpl(connection);
    }

    @NotNull CompletableFuture<Void> pingAsync();

    default void ping() {
        getResultSynchronously(pingAsync());
    }

    @NotNull CompletableFuture<String> getDatabaseVersionAsync();

    default @NotNull String getDatabaseVersion() {
        return getResultSynchronously(getDatabaseVersionAsync());
    }

    @NotNull CompletableFuture<Map<String, String>> infoAsync();

    default Map<String, String> info() {
        return getResultSynchronously(infoAsync());
    }

    CompletableFuture<Void> signInAsync(SurrealAuthCredentials credentials);

    default void signIn(SurrealAuthCredentials credentials) {
        getResultSynchronously(signInAsync(credentials));
    }

    CompletableFuture<Void> useAsync(String namespace, String database);

    default void use(String namespace, String database) {
        getResultSynchronously(useAsync(namespace, database));
    }

    CompletableFuture<Void> setConnectionWideParameterAsync(String key, Object value);

    default void setConnectionWideParameter(String key, Object value) {
        getResultSynchronously(setConnectionWideParameterAsync(key, value));
    }

    CompletableFuture<Void> unsetConnectionWideParameterAsync(String key);

    default void unsetConnectionWideParameter(String key) {
        getResultSynchronously(unsetConnectionWideParameterAsync(key));
    }

    <T> CompletableFuture<List<QueryResult<T>>> queryAsync(String query, Class<T> queryResult, Map<String, Object> params);

    default <T> CompletableFuture<List<QueryResult<T>>> queryAsync(String query, Class<T> queryResult) {
        return queryAsync(query, queryResult, ImmutableMap.of());
    }

    default <T> List<QueryResult<T>> query(String query, Class<T> queryResult, Map<String, Object> params) {
        return getResultSynchronously(queryAsync(query, queryResult, params));
    }

    default <T> List<QueryResult<T>> query(String query, Class<T> queryResult) {
        return getResultSynchronously(queryAsync(query, queryResult));
    }

    <T> CompletableFuture<Optional<T>> querySingleAsync(String query, Class<T> queryResult, Map<String, Object> params);

    default <T> Optional<T> querySingle(String query, Class<T> queryResult, Map<String, Object> params) {
        return getResultSynchronously(querySingleAsync(query, queryResult, params));
    }

    default <T> CompletableFuture<Optional<T>> querySingleAsync(String query, Class<T> queryResult) {
        return querySingleAsync(query, queryResult, ImmutableMap.of());
    }

    default <T> Optional<T> querySingle(String query, Class<T> queryResult) {
        return getResultSynchronously(querySingleAsync(query, queryResult));
    }

    <T> CompletableFuture<List<T>> retrieveAllRecordsFromTableAsync(SurrealTable<T> table);

    default <T> List<T> retrieveAllRecordsFromTable(SurrealTable<T> table) {
        return getResultSynchronously(retrieveAllRecordsFromTableAsync(table));
    }

    <T> CompletableFuture<Optional<T>> retrieveRecordAsync(SurrealTable<T> table, String record);

    default <T> Optional<T> retrieveRecord(SurrealTable<T> table, String record) {
        return getResultSynchronously(retrieveRecordAsync(table, record));
    }

    <T> CompletableFuture<T> createRecordAsync(SurrealTable<T> table, T data);

    default <T> T createRecord(SurrealTable<T> table, T data) {
        return getResultSynchronously(createRecordAsync(table, data));
    }

    <T> CompletableFuture<T> createRecordAsync(SurrealTable<T> table, String record, T data);

    default <T> T createRecord(SurrealTable<T> table, String record, T data) {
        return getResultSynchronously(createRecordAsync(table, record, data));
    }

    <T> CompletableFuture<List<T>> updateAllRecordsInTableAsync(SurrealTable<T> table, T data);

    default <T> List<T> updateAllRecordsInTable(SurrealTable<T> table, T data) {
        return getResultSynchronously(updateAllRecordsInTableAsync(table, data));
    }

    <T> CompletableFuture<T> updateRecordAsync(SurrealTable<T> table, String record, T data);

    default <T> T updateRecord(SurrealTable<T> table, String record, T data) {
        return getResultSynchronously(updateRecordAsync(table, record, data));
    }

    <T> CompletableFuture<List<T>> changeAllRecordsInTableAsync(SurrealTable<T> table, T data);

    default <T> List<T> changeAllRecordsInTable(SurrealTable<T> table, T data) {
        return getResultSynchronously(changeAllRecordsInTableAsync(table, data));
    }

    <T> CompletableFuture<T> changeRecordAsync(SurrealTable<T> table, String record, T data);

    default <T> T changeRecord(SurrealTable<T> table, String record, T data) {
        return getResultSynchronously(changeRecordAsync(table, record, data));
    }

    <T> CompletableFuture<List<T>> patchAllRecordsInTableAsync(SurrealTable<T> table, List<Patch> patches);

    default <T> List<T> patchAllRecordsInTable(SurrealTable<T> table, List<Patch> patches) {
        return getResultSynchronously(patchAllRecordsInTableAsync(table, patches));
    }

    <T> CompletableFuture<T> patchRecordAsync(SurrealTable<T> table, String record, List<Patch> patches);

    default <T> T patchRecord(SurrealTable<T> table, String record, List<Patch> patches) {
        return getResultSynchronously(patchRecordAsync(table, record, patches));
    }

    <T> CompletableFuture<List<T>> deleteAllRecordsInTableAsync(SurrealTable<T> table);

    default <T> List<T> deleteAllRecordsInTable(SurrealTable<T> table) {
        return getResultSynchronously(deleteAllRecordsInTableAsync(table));
    }

    <T> CompletableFuture<T> deleteRecordAsync(SurrealTable<T> table, String record);

    default <T> T deleteRecord(SurrealTable<T> table, String record) {
        return getResultSynchronously(deleteRecordAsync(table, record));
    }

    /**
     * @return the {@link SurrealConnection} this driver is using.
     */
    SurrealConnection getSurrealConnection();

    /**
     * @return the {@link ExecutorService} this driver is using for asynchronous operations.
     */
    ExecutorService getAsyncOperationExecutorService();

}
