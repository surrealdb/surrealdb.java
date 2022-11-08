package com.surrealdb.connection;

import lombok.NonNull;
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
    @NotNull Object @NonNull [] params;

}
