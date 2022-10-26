package com.surrealdb.connection;

import lombok.Value;

/**
 * An internal representation of an RPC request.
 *
 * @author Khalid Alharisi
 */
@Value
class RpcRequest {

    String id;
    String method;
    Object[] params;

}
