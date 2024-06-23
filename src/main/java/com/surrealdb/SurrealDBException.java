package com.surrealdb;

public class SurrealDBException extends RuntimeException {

    SurrealDBException(String message) {
        super(message);
    }

    SurrealDBException(String message, Throwable cause) {
        super(message, cause);
    }

}