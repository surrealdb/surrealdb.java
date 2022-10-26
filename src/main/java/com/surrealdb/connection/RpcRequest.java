package com.surrealdb.connection;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * An internal representation of an RPC request.
 *
 * @author Khalid Alharisi
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
class RpcRequest {

    private final String id;
    private final String method;
    private final Object[] params;

}
