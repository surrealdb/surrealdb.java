package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealException;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.patch.Patch;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Khalid Alharisi
 */
public class SyncSurrealDriver {

    private final AsyncSurrealDriver asyncDriver;

    public SyncSurrealDriver(final SurrealConnection connection) {
        this.asyncDriver = new AsyncSurrealDriver(connection);
    }

    public void ping() {
        getResultSynchronously(asyncDriver.ping());
    }

    public Map<String, String> info() {
        return getResultSynchronously(asyncDriver.info());
    }

    public void signIn(final String username, final String password) {
        getResultSynchronously(asyncDriver.signIn(username, password));
    }

    public String signUp(
            final String namespace,
            final String database,
            final String scope,
            final String email,
            final String password) {
        return getResultSynchronously(
                asyncDriver.signUp(namespace, database, scope, email, password));
    }

    public void authenticate(final String token) {
        getResultSynchronously(asyncDriver.authenticate(token));
    }

    public void invalidate() {
        getResultSynchronously(asyncDriver.invalidate());
    }

    public void use(final String namespace, final String database) {
        getResultSynchronously(asyncDriver.use(namespace, database));
    }

    public void let(final String key, final String value) {
        getResultSynchronously(asyncDriver.let(key, value));
    }

    public <T> List<QueryResult<T>> query(
            final String query, final Map<String, String> args, final Class<? extends T> rowType) {
        return getResultSynchronously(asyncDriver.query(query, args, rowType));
    }

    public <T> List<T> select(final String thing, final Class<? extends T> rowType) {
        return getResultSynchronously(asyncDriver.select(thing, rowType));
    }

    public <T> T create(final String thing, final T data) {
        return getResultSynchronously(asyncDriver.create(thing, data));
    }

    public <T> List<T> update(final String thing, final T data) {
        return getResultSynchronously(asyncDriver.update(thing, data));
    }

    public <T, P> List<T> change(final String thing, final P data, final Class<T> rowType) {
        return getResultSynchronously(asyncDriver.change(thing, data, rowType));
    }

    public void patch(final String thing, final List<Patch> patches) {
        getResultSynchronously(asyncDriver.patch(thing, patches));
    }

    public void delete(final String thing) {
        getResultSynchronously(asyncDriver.delete(thing));
    }

    private <T> T getResultSynchronously(final CompletableFuture<T> completableFuture) {
        try {
            return completableFuture.get();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof SurrealException) {
                throw (SurrealException) e.getCause();
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
