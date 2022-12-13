package com.surrealdb.client.bidirectional.rpc;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * An internal representation of an RPC request.
 *
 * @author Khalid Alharisi
 */
@Value
public class RpcRequest {

    @SerializedName("id")
    @NotNull String id;

    @SerializedName("method")
    @NotNull String method;

    @SerializedName("params")
    @NotNull Object @NotNull [] parameters;

}
