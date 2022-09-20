package com.surrealdb.java.client.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class RpcRequest {
    private final String id;
    private final String method;
    private final Object[] params;

    public RpcRequest(String method, Object... params) {
        id = UUID.randomUUID().toString(); // TODO: change to a faster random ID
        this.method = method;
        this.params = params;
    }
}
