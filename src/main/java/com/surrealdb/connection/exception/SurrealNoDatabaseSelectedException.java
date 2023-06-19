package com.surrealdb.connection.exception;

import com.surrealdb.connection.model.RpcResponse;

/**
 * @author Khalid Alharisi
 */
public class SurrealNoDatabaseSelectedException extends SurrealException {
    public SurrealNoDatabaseSelectedException(RpcResponse.Error error) {
        super(error, null);
    }
}
