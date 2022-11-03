package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealException;
import com.surrealdb.driver.auth.*;
import com.surrealdb.driver.patch.Patch;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * A synchronous SurrealDB driver. This driver is used in conjunction with a
 * {@link SurrealConnection} to communicate with the server. The driver provides methods for
 * signing in to the server, executing queries, and subscribing to data (coming soon). All methods in this
 * driver are synchronous and will block until the operation is finished.
 *
 * @author Khalid Alharisi
 */
public class SyncSurrealDriver implements SurrealDriver {

    private final AsyncSurrealDriver asyncDriver;

    /**
     * Creates a new {@link SyncSurrealDriver} instance using the provided {@link SurrealConnection} and
     * {@link SurrealDriverSettings}. The connection must connect to a SurrealDB server before any
     * driver methods are called.
     *
     * @param connection The connection to use for communicating with the server.
     * @param settings   The settings this driver should use.
     */
    public SyncSurrealDriver(SurrealConnection connection, SurrealDriverSettings settings) {
        asyncDriver = new AsyncSurrealDriver(connection, settings);
    }

    /**
     * Creates a new {@link SyncSurrealDriver} instance using the provided connection. The connection
     * must connect to a SurrealDB server before any driver methods are called.
     *
     * @param connection The connection to use for communication with the server
     */
    public SyncSurrealDriver(SurrealConnection connection) {
        this.asyncDriver = new AsyncSurrealDriver(connection);
    }

    /**
     * Pings the SurrealDB server. This method will block until the server responds.
     */
    public void ping() {
        getResultSynchronously(asyncDriver.ping());
    }

    /**
     * Asks the database for its version. As of version v1.0.0-beta.9, the returned string
     * will be in the format {@code 'PKG_NAME-PKG_VERSION}'. This method will block until the server responds.
     *
     * @return The version of the SurrealDB server
     */
    public String getDatabaseVersion() {
        return getResultSynchronously(asyncDriver.getDatabaseVersion());
    }

    public Map<String, String> info() {
        return getResultSynchronously(asyncDriver.info());
    }

    /**
     * Signs in to the server using the provided credentials. This method will block until the server
     * responds.
     *
     * @param credentials The credentials to sign in with
     * @see SurrealRootCredentials#from(String, String)
     * @see SurrealNamespaceCredentials#from(String, String, String)
     * @see SurrealDatabaseCredentials#from(String, String, String, String)
     * @see SurrealScopeCredentials#from(String, String, String)
     */
    public void signIn(SurrealAuthCredentials credentials) {
        getResultSynchronously(asyncDriver.signIn(credentials));
    }

    /**
     * Selects a namespace and database to use for subsequent queries. This method will block until
     * the server responds.
     *
     * @param namespace The namespace to use
     * @param database  The database to use
     */
    public void use(String namespace, String database) {
        getResultSynchronously(asyncDriver.use(namespace, database));
    }

    /**
     * Sets a connection wide key-value pair. The value can be used in queries with the
     * syntax <i>$key</i>. This method will block until the server responds.
     * This method will block until the server responds.
     *
     * @param key   The parameter key
     * @param value The parameter value
     */
    public void setConnectionWideParameter(String key, Object value) {
        getResultSynchronously(asyncDriver.setConnectionWideParameter(key, value));
    }

    /**
     * Unsets and clears a connection wide key-value pair. This method will block until the server
     * responds.
     *
     * @param key The parameter key
     */
    public void unsetConnectionWideParameter(String key) {
        getResultSynchronously(asyncDriver.unsetConnectionWideParameter(key));
    }

    public <T> List<QueryResult<T>> query(String query, Class<T> rowType, Map<String, Object> args) {
        return getResultSynchronously(asyncDriver.query(query, rowType, args));
    }

    public <T> Optional<T> querySingle(String query, Class<T> rowType, Map<String, Object> args) {
        return getResultSynchronously(asyncDriver.querySingle(query, rowType, args));
    }

    public <T> List<T> retrieveAllRecordsFromTable(SurrealTable<T> table) {
        return getResultSynchronously(asyncDriver.retrieveAllRecordsFromTable(table));
    }

    public <T> Optional<T> retrieveRecordFromTable(SurrealTable<T> table, String record) {
        return getResultSynchronously(asyncDriver.retrieveRecordFromTable(table, record));
    }

    public <T> T create(String thing, T data) {
        return getResultSynchronously(asyncDriver.create(thing, data));
    }

    public <T> List<T> update(String thing, T data) {
        return getResultSynchronously(asyncDriver.update(thing, data));
    }

    public <T, P> List<T> change(String thing, P data, Class<T> rowType) {
        return getResultSynchronously(asyncDriver.change(thing, data, rowType));
    }

    public void patch(String thing, List<Patch> patches) {
        getResultSynchronously(asyncDriver.patch(thing, patches));
    }

    public void delete(String thing) {
        getResultSynchronously(asyncDriver.delete(thing));
    }

    private <T> T getResultSynchronously(CompletableFuture<T> completableFuture) {
        try {
            return completableFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof SurrealException) {
                throw (SurrealException) e.getCause();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public SurrealConnection getSurrealConnection() {
        return asyncDriver.getSurrealConnection();
    }

    @Override
    public ExecutorService getAsyncOperationExecutorService() {
        return asyncDriver.getAsyncOperationExecutorService();
    }
}
