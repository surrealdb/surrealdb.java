package com.surrealdb.client;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * An internal representation of an RPC request.
 *
 * @author Khalid Alharisi
 */
@Value
class RpcRequest {

    @NotNull String id;
    @NotNull String method;
    @NotNull Object @NotNull [] params;

}