package com.surrealdb.java;

import com.surrealdb.java.client.SurrealClient;
import com.surrealdb.java.model.SignIn;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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

}
