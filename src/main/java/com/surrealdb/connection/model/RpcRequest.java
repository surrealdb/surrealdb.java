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

	public RpcRequest(String id, String method, Object... params) {
		this.id = id;
		this.method = method;
		this.params = params;
	}
}
