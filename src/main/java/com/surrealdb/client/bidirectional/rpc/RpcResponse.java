package com.surrealdb.client.bidirectional.rpc;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
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
public class RpcResponse {

    @SerializedName("id")
    @NotNull String id;

    @SerializedName("result")
    @NotNull JsonElement result;

    @SerializedName("error")
    @Nullable Error error;

    public @NotNull Optional<Error> getError() {
        return Optional.ofNullable(error);
    }

    /**
     * An internal representation of an RPC error.
     */
    @Value
    public static class Error {

        @SerializedName("code")
        int code;

        @SerializedName(value="message", alternate={"description"})
        @NotNull String message;

    }
}
