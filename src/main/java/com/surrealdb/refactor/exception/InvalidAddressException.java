package com.surrealdb.refactor.exception;

import lombok.ToString;

import java.net.URI;
import java.util.Optional;

@ToString
public class InvalidAddressException extends SurrealDBException {

    private final URI address;
    private final InvalidAddressExceptionCause causeType;

    public InvalidAddressException(URI address, InvalidAddressExceptionCause causeType, String message) {
        super(message);
        this.address = address;
        this.causeType = causeType;
    }

    public Optional<URI> getAddress() {
        return Optional.ofNullable(address);
    }

    public InvalidAddressExceptionCause getCauseType() {
        return causeType;
    }

}
