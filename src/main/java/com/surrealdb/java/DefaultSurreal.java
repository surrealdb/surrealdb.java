package com.surrealdb.java;

import com.google.gson.reflect.TypeToken;
import com.surrealdb.java.connection.SurrealConnection;
import com.surrealdb.java.connection.SurrealWebSocketConnection;
import com.surrealdb.java.connection.exception.SurrealException;
import com.surrealdb.java.model.QueryResult;
import com.surrealdb.java.model.SignIn;
import com.surrealdb.java.model.patch.Patch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DefaultSurreal implements Surreal {

    private final SurrealConnection connection;

    public DefaultSurreal(String host, int port, int connectionTimeoutSeconds){
        connection = new SurrealWebSocketConnection(host, port);
        connection.connect(connectionTimeoutSeconds);
    }

    @SneakyThrows
    @Override
    public void signIn(String username, String password){
        CompletableFuture<?> future = connection.rpc(null, "signin", new SignIn(username, password));
        getResultSynchronously(future);
        log.debug("Signed-in successfully");
    }

    @SneakyThrows
    @Override
    public void use(String namespace, String database){
        CompletableFuture<?> future = connection.rpc(null, "use", namespace, database);
        getResultSynchronously(future);
        log.debug("You are now using namespace={} database={}", namespace, database);
    }

    @SneakyThrows
    @Override
    public void let(String key, String value){
        CompletableFuture<?> future = connection.rpc(null, "let", key, value);
        getResultSynchronously(future);
        log.debug("Set key={} to value={}", key, value);
    }

    @SneakyThrows
    @Override
    public <T> List<QueryResult<T>> query(String query, Map<String, String> args, Class<? extends T> rowClass){
        Type queryResultType = TypeToken.getParameterized(QueryResult.class, rowClass).getType();
        Type resultType = TypeToken.getParameterized(List.class, queryResultType).getType();

        CompletableFuture<List<QueryResult<T>>> future = connection.rpc(resultType, "query", query, args);
        List<QueryResult<T>> result = getResultSynchronously(future);
        log.debug("query result {}", result);
        return result;
    }

    @SneakyThrows
    @Override
    public <T> List<T> select(String thing, Class<? extends T> rowType){
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();

        CompletableFuture<List<T>> future = connection.rpc(resultType, "select", thing);
        List<T> result = getResultSynchronously(future);
        log.debug("selected {}", result);
        return result;
    }

    @SneakyThrows
    @Override
    public <T> T create(String thing, T data){
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();

        CompletableFuture<List<T>> future = connection.rpc(resultType, "create", thing, data);
        List<T> result = getResultSynchronously(future);
        log.debug("created {}", result.get(0));
        return result.get(0);
    }

    @SneakyThrows
    @Override
    public <T> List<T> update(String thing, T data){
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();

        CompletableFuture<List<T>> future = connection.rpc(resultType, "update", thing, data);
        List<T> result = getResultSynchronously(future);
        log.debug("updated {}", result);
        return result;
    }

    @SneakyThrows
    @Override
    public <T, P> List<T> change(String thing, P data, Class<T> rowType){
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();

        CompletableFuture<List<T>> future = connection.rpc(resultType, "change", thing, data);
        List<T> result = getResultSynchronously(future);
        log.debug("changed {}", result);
        return result;
    }

    @SneakyThrows
    @Override
    public void patch(String thing, List<Patch> patches){
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();

        CompletableFuture<List<Object>> future = connection.rpc(resultType, "modify", thing, patches);
        List<Object> result = getResultSynchronously(future);
        log.debug("patched {}", result.size());
    }

    @SneakyThrows
    @Override
    public void delete(String thing){
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();

        CompletableFuture<List<Object>> future = connection.rpc(resultType, "delete", thing);
        List<Object> result = getResultSynchronously(future);
        log.debug("deleted {}", result.size());
    }

    private  <T> T getResultSynchronously(CompletableFuture<T> completableFuture){
        try {
            return completableFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if(e.getCause() instanceof SurrealException){
                throw (SurrealException) e.getCause();
            }else{
                throw new RuntimeException(e);
            }
        }
    }
}
