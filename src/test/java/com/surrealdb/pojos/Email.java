package com.surrealdb.pojos;

import java.util.Objects;

public class Email {

    public String address;
    public Name name;

    public Email() {
    }

    public Email(String address, Name name) {
        this.address = address;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Email email = (Email) o;
        return email.address.equals(address) && email.name.equals(name);
    }

    @Override
    public String toString() {
        return "name: " + this.name + ", address: " + this.address;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address);
    }
}
