package com.surrealdb.connection.model;

import com.google.gson.JsonElement;

/**
 * @author Khalid Alharisi
 */
public record RpcResponse(
        String id, JsonElement result, com.surrealdb.connection.model.RpcResponse.Error error) {
    public record Error(int code, String message) {}
}
