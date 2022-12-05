package com.surrealdb.client.listener;

@FunctionalInterface
public interface SurrealExceptionListener {

    void onException(Exception e);

}
