package com.surrealdb.connection;

import com.google.gson.JsonElement;
import lombok.*;

/**
 * An internal representation of an RPC response.
 *
 * @author Khalid Alharisi
 */
@Value
class RpcResponse {

    String id;
    JsonElement result;
    Error error;

    @Value
    static class Error {
        int code;
        String message;
    }

    public boolean isSuccessful() {
        return error == null;
    }
}
