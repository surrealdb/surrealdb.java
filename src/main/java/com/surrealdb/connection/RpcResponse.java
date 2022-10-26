package com.surrealdb.connection;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * An internal representation of an RPC response.
 *
 * @author Khalid Alharisi
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
class RpcResponse {

    private final String id;
    private final JsonElement result;
    private final Error error;

    @Getter
    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    static class Error {
        private int code;
        private String message;
    }

    public boolean isSuccessful() {
        return error == null;
    }
}
