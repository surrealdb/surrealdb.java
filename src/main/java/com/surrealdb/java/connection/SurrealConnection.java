package com.surrealdb.java.connection;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

public interface SurrealConnection {

    void connect(int timeoutSeconds);

    void disconnect();

    <T> CompletableFuture<T> rpc(Type resultType, String method, Object... params);

}
