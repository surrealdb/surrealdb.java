package com.surrealdb.java;

import com.google.gson.reflect.TypeToken;
import com.surrealdb.java.client.SurrealClient;
import com.surrealdb.java.model.SignIn;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;
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
    public <T> T create(String thing, T data){
        Type resultType = TypeToken.getParameterized(List.class, data.getClass()).getType();

        CompletableFuture<List<T>> future = client.rpc(resultType, "create", thing, data);
        List<T> result = future.get();
        log.debug("created {}", result.get(0));
        return result.get(0);
    }

}
