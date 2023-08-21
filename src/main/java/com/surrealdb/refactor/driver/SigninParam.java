package com.surrealdb.refactor.driver;

import lombok.Getter;

@Getter
public class SigninParam {
    private final String user;
    private final String pass;

    public SigninParam(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }
}
