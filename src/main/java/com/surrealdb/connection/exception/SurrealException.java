package com.surrealdb.connection.exception;

import com.surrealdb.connection.model.RpcResponse;

import java.util.Optional;

/**
 * @author Khalid Alharisi
 */
public class SurrealException extends RuntimeException {

    private final RpcResponse.Error response;

    public SurrealException() {
        this(null, null);
    }

    public SurrealException(String message) {
        this(null, message);
    }

    public SurrealException(RpcResponse.Error response, String message) {
        super(message);
        this.response = response;
    }

    public Optional<RpcResponse.Error> getResponse() {
        return Optional.ofNullable(response);
    }
}
