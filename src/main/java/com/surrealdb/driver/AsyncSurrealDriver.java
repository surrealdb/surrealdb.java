package com.surrealdb.driver;

import com.google.gson.reflect.TypeToken;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.patch.Patch;
import com.surrealdb.driver.model.SignIn;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Khalid Alharisi
 */
public class AsyncSurrealDriver {

    private final SurrealConnection connection;

    public AsyncSurrealDriver(SurrealConnection connection){
        this.connection = connection;
    }

    public CompletableFuture<?> ping(){
        return connection.rpc(Boolean.class, "ping");
    }

    public CompletableFuture<Map<String, String>> info(){
        Type resultType = TypeToken.getParameterized(Map.class, String.class, String.class).getType();
        return connection.rpc(resultType, "info");
    }

    public CompletableFuture<?> signIn(String username, String password){
        return connection.rpc(null, "signin", new SignIn(username, password));
    }

    public CompletableFuture<?> use(String namespace, String database){
        return connection.rpc(null, "use", namespace, database);
    }

    public CompletableFuture<?> let(String key, String value){
        return connection.rpc(null, "let", key, value);
    }

    public <T> CompletableFuture<List<QueryResult<T>>> query(String query, Map<String, String> args, Class<? extends T> rowType){
        Type queryResultType = TypeToken.getParameterized(QueryResult.class, rowType).getType();
        Type resultType = TypeToken.getParameterized(List.class, queryResultType).getType();
        return connection.rpc(resultType, "query", query, args);
    }

    public <T> CompletableFuture<List<T>> select(String thing, Class<? extends T> rowType){
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return connection.rpc(resultType, "select", thing);
    }

    public <T> CompletableFuture<T> create(String thing, T data){
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        CompletableFuture<T> finalFuture = new CompletableFuture<>();

        CompletableFuture<List<T>> createFuture = connection.rpc(resultType, "create", thing, data);
        createFuture.whenComplete((list, throwable) -> {
            if(throwable != null){
                finalFuture.completeExceptionally(throwable);
            }else{
                finalFuture.complete(list.get(0));
            }
        });

        return finalFuture;
    }

    public <T> CompletableFuture<List<T>> update(String thing, T data){
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();
        return connection.rpc(resultType, "update", thing, data);
    }

    public <T, P> CompletableFuture<List<T>> change(String thing, P data, Class<T> rowType){
        Type resultType = TypeToken.getParameterized(List.class, rowType).getType();
        return connection.rpc(resultType, "change", thing, data);
    }

    public CompletableFuture<?> patch(String thing, List<Patch> patches){
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return connection.rpc(resultType, "modify", thing, patches);
    }

    public CompletableFuture<?> delete(String thing){
        Type resultType = TypeToken.getParameterized(List.class, Object.class).getType();
        return connection.rpc(resultType, "delete", thing);
    }

}
