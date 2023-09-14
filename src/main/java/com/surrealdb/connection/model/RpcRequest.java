package com.surrealdb.connection.model;

import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
@Getter
public class RpcRequest {
    private final String id;
    private final String method;
    private final Object[] params;

    public RpcRequest(final String id, final String method, final Object... params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }
}
