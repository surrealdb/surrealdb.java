package com.surrealdb.connection;

import com.google.gson.JsonElement;
import lombok.Value;

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

    /**
     * @return true if the response is an error, false if successful
     */
    public boolean isSuccessful() {
        return error == null;
    }

    /**
     * An internal representation of an RPC error.
     */
    @Value
    static class Error {
        int code;
        String message;
    }
}
