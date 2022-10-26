package com.surrealdb.connection.model;

import com.google.gson.JsonElement;
import lombok.Data;

/**
 * @author Khalid Alharisi
 */
@Data
public class RpcResponse {

    private final String id;
    private final JsonElement result;
    private final Error error;

    @Data
    public static class Error {
        private int code;
        private String message;
    }

}
