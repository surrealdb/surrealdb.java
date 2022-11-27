package com.surrealdb.driver;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.patch.Patch;
import com.surrealdb.driver.sql.QueryResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static com.surrealdb.driver.InternalDriverUtils.getResultSynchronously;

/**
 * @author Khalid Alharisi
 */
public interface SurrealDriver {

    static @NotNull SurrealDriver create(@NotNull SurrealConnection connection, @NotNull SurrealDriverSettings settings) {
        return RegularSurrealDriver.create(connection, settings);
    }

    static @NotNull SurrealDriver create(@NotNull SurrealConnection connection) {
        return create(connection, SurrealDriverSettings.DEFAULT);
    }

    @NotNull CompletableFuture<Void> pingAsync();

    default void ping() {
        getResultSynchronously(pingAsync());
    }

    @NotNull CompletableFuture<String> databaseVersionAsync();

    default @NotNull String databaseVersion() {
        return getResultSynchronously(databaseVersionAsync());
    }

    @NotNull CompletableFuture<Map<String, String>> infoAsync();

    default @NotNull Map<String, String> info() {
        return getResultSynchronously(infoAsync());
    }

    @NotNull CompletableFuture<Void> signInAsync(@NotNull SurrealAuthCredentials credentials);

    default void signIn(@NotNull SurrealAuthCredentials credentials) {
        getResultSynchronously(signInAsync(credentials));
    }

    @NotNull CompletableFuture<Void> useAsync(@NotNull String namespace, @NotNull String database);

    default void use(@NotNull String namespace, @NotNull String database) {
        getResultSynchronously(useAsync(namespace, database));
    }

    CompletableFuture<Void> setConnectionWideParameterAsync(@NotNull String key, @NotNull Object value);

    default void setConnectionWideParameter(@NotNull String key, @NotNull Object value) {
        getResultSynchronously(setConnectionWideParameterAsync(key, value));
    }

    CompletableFuture<Void> unsetConnectionWideParameterAsync(@NotNull String key);

    default void unsetConnectionWideParameter(@NotNull String key) {
        getResultSynchronously(unsetConnectionWideParameterAsync(key));
    }

    <T> CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args);

    <T> CompletableFuture<Optional<T>> sqlSingleAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args);

    <T> CompletableFuture<List<T>> sqlFirstAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args);

    default <T> CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlAsync(query, queryResult, ImmutableMap.of());
    }

    default <T> List<QueryResult<T>> sql(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlAsync(query, queryResult, args));
    }

    default <T> List<QueryResult<T>> sql(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlAsync(query, queryResult));
    }

    default <T> Optional<T> sqlSingle(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlSingleAsync(query, queryResult, args));
    }

    default <T> Optional<T> sqlSingle(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlSingleAsync(query, queryResult));
    }

    default <T> List<T> sqlFirst(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        return getResultSynchronously(sqlFirstAsync(query, queryResult, args));
    }

    default <T> CompletableFuture<List<T>> sqlFirstAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlFirstAsync(query, queryResult, ImmutableMap.of());
    }

    default <T> List<T> sqlFirst(@NotNull String query, @NotNull Class<T> queryResult) {
        return getResultSynchronously(sqlFirstAsync(query, queryResult));
    }

    default <T> CompletableFuture<Optional<T>> sqlSingleAsync(@NotNull String query, @NotNull Class<T> queryResult) {
        return sqlSingleAsync(query, queryResult, ImmutableMap.of());
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
