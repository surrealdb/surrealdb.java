package com.surrealdb.refactor.exception;

import java.net.URI;
import java.util.Optional;

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

    @Override
    public String toString() {
        return "InvalidAddressException{" +
            "address=" + address +
            ", causeType=" + causeType +
            ", message=" + getMessage() +
            '}';
    }
}

