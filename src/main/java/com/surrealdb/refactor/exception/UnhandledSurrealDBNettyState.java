package com.surrealdb.refactor.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UnhandledSurrealDBNettyState extends SurrealDBException {
    private final String reasonUnhandled;
    private final String causeOfError;

    public UnhandledSurrealDBNettyState(String reasonUnhandled, String causeOfError) {
        super(causeOfError);
        this.reasonUnhandled = reasonUnhandled;
        this.causeOfError = causeOfError;
    }
}
