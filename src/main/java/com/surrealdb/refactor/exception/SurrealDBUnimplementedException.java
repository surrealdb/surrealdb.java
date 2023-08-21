package com.surrealdb.refactor.exception;

import lombok.ToString;

import java.net.MalformedURLException;
import java.net.URL;

@ToString
public class SurrealDBUnimplementedException extends SurrealDBException {

    private final URL ticketLink;
    private final String message; // Necessary for lombok toString

    public SurrealDBUnimplementedException(URL ticket, String message) {
        super(message);
        ticketLink = ticket;
        this.message = message;
    }

    public URL getTicketLink() {
        return ticketLink;
    }

    //----------------------------------------------------------------
    // Builder

    public static WithTicket withTicket(String url) {
        try {
            URL ticket = new URL(url);
            return new WithTicket(ticket);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static class WithTicket {
        private final URL ticket;
        WithTicket(URL ticket) {
            this.ticket = ticket;
        }

        public SurrealDBUnimplementedException withMessage(String message) {
            return new SurrealDBUnimplementedException(ticket, message);
        }
    }
}
