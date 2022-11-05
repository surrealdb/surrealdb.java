package com.surrealdb.connection;

import com.google.gson.JsonElement;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An internal representation of an RPC response.
 *
 * @author Khalid Alharisi
 */
@Value
class RpcResponse {

    @NotNull String id;
    @NotNull JsonElement result;
    @Nullable Error error;

    public @NotNull Optional<Error> getError() {
        return Optional.ofNullable(error);
    }

    /**
     * An internal representation of an RPC error.
     */
    @Value
    static class Error {
        int code;
        @NotNull String message;
    }
}
