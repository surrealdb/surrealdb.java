package com.surrealdb.refactor.exception;

import lombok.Getter;
import lombok.ToString;

@ToString
public class UnknownResponseToRequest extends SurrealDBException {
    @Getter private final String requestID;
    @Getter private final String message;

    public UnknownResponseToRequest(String requestID, String message) {
        super(message);
        this.requestID = requestID;
        this.message = message;
    }
}
