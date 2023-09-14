package com.surrealdb.driver;

import com.google.gson.reflect.TypeToken;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.SignIn;
import com.surrealdb.driver.model.SignUp;
import com.surrealdb.driver.model.patch.Patch;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Khalid Alharisi
 */
public class AsyncSurrealDriver {

    private final SurrealConnection connection;

    public AsyncSurrealDriver(final SurrealConnection connection) {
        this.connection = connection;
    }

    public CompletableFuture<?> ping() {
        return this.connection.rpc(Boolean.class, "ping");
    }

    public CompletableFuture<Map<String, String>> info() {
        final Type resultType =
                TypeToken.getParameterized(Map.class, String.class, String.class).getType();
        return this.connection.rpc(resultType, "info");
    }

    public CompletableFuture<?> signIn(final String username, final String password) {
        return this.connection.rpc(null, "signin", new SignIn(username, password));
    }

    public CompletableFuture<String> signUp(
            final String namespace, final String database, final String scope, final String email, final String password) {
        final Type resultType = TypeToken.getParameterized(String.class).getType();

        final SignUp userToBeCreated = new SignUp(namespace, database, scope, email, password);

        return this.connection.rpc(resultType, "signup", userToBeCreated);
    }

    public CompletableFuture<?> authenticate(final String token) {
        return this.connection.rpc(null, "authenticate", token);
    }

    public CompletableFuture<?> invalidate() {
        return this.connection.rpc(null, "invalidate");
    }

    public CompletableFuture<?> use(final String namespace, final String database) {
        return this.connection.rpc(null, "use", namespace, database);
    }

    public CompletableFuture<?> let(final String key, final String value) {
        return this.connection.rpc(null, "let", key, value);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> query(
            final String query, final Map<String, String> args, final Class<? extends T> rowType) {
        final Type queryResultType = TypeToken.getParameterized(QueryResult.class, rowType).getType();
        final Type resultType      = TypeToken.getParameterized(List.class, queryResultType).getType();
        return this.connection.rpc(resultType, "query", query, args);
    }

    public <T> CompletableFuture<List<T>> select(final String thing, final Class<? extends T> rowType) {
        final Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return this.connection.rpc(resultType, "select", thing);
    }

    public <T> CompletableFuture<T> create(final String thing, final T data) {
        final Type                 resultType  = TypeToken.getParameterized(List.class, data.getClass()).getType();
        final CompletableFuture<T> finalFuture = new CompletableFuture<>();

        final CompletableFuture<List<T>> createFuture = this.connection.rpc(resultType, "create", thing, data);
        createFuture.whenComplete(
                (list, throwable) -> {
                    if (throwable != null) {
                        finalFuture.completeExceptionally(throwable);
                    } else {
                        finalFuture.complete(list.get(0));
                    }
                });

        return finalFuture;
    }

    public <T> CompletableFuture<List<T>> update(final String thing, final T data) {
        final Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        return this.connection.rpc(resultType, "update", thing, data);
    }

    public <T, P> CompletableFuture<List<T>> change(final String thing, final P data, final Class<T> rowType) {
        final Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return this.connection.rpc(resultType, "change", thing, data);
    }

    public CompletableFuture<?> patch(final String thing, final List<Patch> patches) {
        final Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return this.connection.rpc(resultType, "modify", thing, patches);
    }

    public CompletableFuture<?> delete(final String thing) {
        final Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return this.connection.rpc(resultType, "delete", thing);
    }
}
