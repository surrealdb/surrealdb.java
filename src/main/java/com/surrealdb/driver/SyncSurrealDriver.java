package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealException;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.patch.Patch;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * A synchronous SurrealDB driver. This driver is used in conjunction with a
 * {@link SurrealConnection} to communicate with the server. The driver provides methods for
 * signing in to the server executing queries, and subscribing to data (coming soon). All methods in this
 * class are synchronous and will block until the operation is finished.
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

    public Map<String, String> info() {
        return getResultSynchronously(asyncDriver.info());
    }

    public void signIn(String username, String password, String namespace, String database) {
        getResultSynchronously(asyncDriver.signIn(username, password, namespace, database));
    }

    public void signIn(String username, String password, String namespace, String database, String token) {
        getResultSynchronously(asyncDriver.signIn(username, password, namespace, database, token));
    }

    /**
     * Signs in to the SurrealDB server. This method will block until the server responds.
     *
     * @param username The username to sign in with
     * @param password The password to sign in with
     */
    public void signIn(String username, String password) {
        getResultSynchronously(asyncDriver.signIn(username, password));
    }

    public void use(String namespace, String database) {
        getResultSynchronously(asyncDriver.use(namespace, database));
    }

    public void let(String key, String value) {
        getResultSynchronously(asyncDriver.let(key, value));
    }

    public <T> List<QueryResult<T>> query(String query, Map<String, String> args, Class<? extends T> rowType) {
        return getResultSynchronously(asyncDriver.query(query, args, rowType));
    }

    public <T> Optional<T> querySingle(String query, Map<String, String> args, Class<? extends T> rowType) {
        return getResultSynchronously(asyncDriver.querySingle(query, args, rowType));
    }

    public <T> List<T> select(String thing, Class<? extends T> rowType) {
        return getResultSynchronously(asyncDriver.select(thing, rowType));
    }

    public <T> Optional<T> selectSingle(String thing, Class<? extends T> rowType) {
        return getResultSynchronously(asyncDriver.selectSingle(thing, rowType));
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
