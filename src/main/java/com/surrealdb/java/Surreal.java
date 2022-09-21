package com.surrealdb.java;

import com.google.gson.reflect.TypeToken;
import com.surrealdb.java.client.SurrealClient;
import com.surrealdb.java.model.QueryResult;
import com.surrealdb.java.model.SignIn;
import com.surrealdb.java.model.patch.Patch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Surreal {

    private final SurrealClient client;

    public Surreal(String host, int port){
        client = new SurrealClient(host, port);
    }

    @SneakyThrows
    public void signIn(String username, String password){
        CompletableFuture<?> future = client.rpc(null, "signin", new SignIn(username, password));
        future.get();
        log.debug("Signed-in successfully");
    }

    @SneakyThrows
    public void use(String namespace, String database){
        CompletableFuture<?> future = client.rpc(null, "use", namespace, database);
        future.get();
        log.debug("You are now using namespace={} database={}", namespace, database);
    }

    @SneakyThrows
    public void let(String key, String value){
        CompletableFuture<?> future = client.rpc(null, "let", key, value);
        future.get();
        log.debug("Set key={} to value={}", key, value);
    }

    @SneakyThrows
    public <T> List<QueryResult<T>> query(String query, Map<String, String> args, Class<? extends T> rowClass){
        Type queryResultType = TypeToken.getParameterized(QueryResult.class, rowClass).getType();
        Type resultType = TypeToken.getParameterized(List.class, queryResultType).getType();

        CompletableFuture<List<QueryResult<T>>> future = client.rpc(resultType, "query", query, args);
        List<QueryResult<T>> result = future.get();
        log.debug("query result {}", result);
        return result;
    }

    @SneakyThrows
    public <T> List<T> select(String thing, Class<? extends T> rowType){
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();

        CompletableFuture<List<T>> future = client.rpc(resultType, "select", thing);
        List<T> result = future.get();
        log.debug("selected {}", result);
        return result;
    }

    @SneakyThrows
    public <T> T create(String thing, T data){
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();

        CompletableFuture<List<T>> future = client.rpc(resultType, "create", thing, data);
        List<T> result = future.get();
        log.debug("created {}", result.get(0));
        return result.get(0);
    }

    @SneakyThrows
    public <T> List<T> update(String thing, T data){
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();

        CompletableFuture<List<T>> future = client.rpc(resultType, "update", thing, data);
        List<T> result = future.get();
        log.debug("updated {}", result);
        return result;
    }

    @SneakyThrows
    public <T, P> List<T> change(String thing, P data, Class<T> rowType){
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();

        CompletableFuture<List<T>> future = client.rpc(resultType, "change", thing, data);
        List<T> result = future.get();
        log.debug("changed {}", result);
        return result;
    }

    @SneakyThrows
    public void patch(String thing, List<Patch> patches){
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();

        CompletableFuture<List<Object>> future = client.rpc(resultType, "modify", thing, patches);
        List<Object> result = future.get();
        log.debug("patched {}", result.size());
    }

    @SneakyThrows
    public void delete(String thing){
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();

        CompletableFuture<List<Object>> future = client.rpc(resultType, "delete", thing);
        List<Object> result = future.get();
        log.debug("deleted {}", result.size());
    }

}
