package com.surrealdb.refactor.types;

import lombok.Getter;

@Getter
public class Credentials {

    private final String username;
    private final String password;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
