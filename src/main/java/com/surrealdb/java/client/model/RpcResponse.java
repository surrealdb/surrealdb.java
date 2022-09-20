package com.surrealdb.java.client.model;

import lombok.Data;

@Data
public class RpcResponse {

    @Data
    public static class Error {
        private int code;
        private String message;
    }

    private final String id;
    private final Object result;
    private final Error error;

}
