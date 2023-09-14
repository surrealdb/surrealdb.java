package com.surrealdb.connection.model;

import com.google.gson.JsonElement;
import lombok.Data;

/**
 * @author Khalid Alharisi
 */
public record RpcResponse(String id, JsonElement result, RpcResponse.Error error) {

    @Data
    public static class Error {
        private int code;
        private String message;
    }
}
