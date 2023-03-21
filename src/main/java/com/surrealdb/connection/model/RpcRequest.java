package com.surrealdb.connection.model;



/**
 * @author Khalid Alharisi
 */
public record RpcRequest(String id, String method, Object... params) {}
