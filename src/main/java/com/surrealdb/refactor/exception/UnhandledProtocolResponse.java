package com.surrealdb.refactor.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UnhandledProtocolResponse extends SurrealDBException {
    private final String message;

    public UnhandledProtocolResponse(String message) {
        super(message);
        this.message = message;
    }
}
