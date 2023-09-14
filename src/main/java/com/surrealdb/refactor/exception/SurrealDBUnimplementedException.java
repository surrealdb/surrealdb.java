package com.surrealdb.refactor.exception;

import java.net.MalformedURLException;
import java.net.URL;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class SurrealDBUnimplementedException extends SurrealDBException {

    private final URL ticketLink;
    private final String message; // Necessary for lombok toString

    public SurrealDBUnimplementedException(final String ticket, final String message) {
        super(message);
        try {
            this.ticketLink = new URL(ticket);
        } catch (final MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.message = message;
    }
}
