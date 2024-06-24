package com.surrealdb.pojos;

public class Email {

    public String address;
    public Name name;

    public Email() {
    }

    public Email(String address, Name name) {
        this.address = address;
        this.name = name;
    }
}
