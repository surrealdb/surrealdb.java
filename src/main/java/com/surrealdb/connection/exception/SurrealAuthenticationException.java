package com.surrealdb.connection.exception;

import com.surrealdb.connection.model.RpcResponse;

/**
 * @author Khalid Alharisi
 */
public class SurrealAuthenticationException extends SurrealException {
    public SurrealAuthenticationException(RpcResponse.Error error) {
        super(error, null);
    }
}
