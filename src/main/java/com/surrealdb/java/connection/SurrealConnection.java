package com.surrealdb.java.connection;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

public interface SurrealConnection {

    <T> CompletableFuture<T> rpc(Type resultType, String method, Object... params);

}
