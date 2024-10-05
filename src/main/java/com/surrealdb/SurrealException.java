package com.surrealdb;

public class SurrealException extends RuntimeException {

    SurrealException(String message) {
        super(message);
    }

    SurrealException(String message, Throwable cause) {
        super(message, cause);
    }

}