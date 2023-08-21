package com.surrealdb.refactor.driver;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class SigninMessage {
    private final String method = "signin";
    private final String requestID;

    private final List<SigninParam> params;

    public SigninMessage(String requestID, String username, String password) {
        this.requestID = requestID;
        this.params = Arrays.asList(
            new SigninParam(username, password)
        );
    }
}
