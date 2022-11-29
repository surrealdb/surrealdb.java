package com.surrealdb.connection;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * An internal representation of an RPC request.
 *
 * @author Khalid Alharisi
 */
@Value
public
class RpcRequest {

    @NotNull String id;
    @NotNull String method;
    @NotNull Object @NotNull [] params;

}
