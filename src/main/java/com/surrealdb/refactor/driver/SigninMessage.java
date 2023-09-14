package com.surrealdb.refactor.driver;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public class SigninMessage {
    private final String method = "signin";
    private final String id;

    private final List<SigninParam> params;

    public SigninMessage(final String requestID, final String username, final String password) {
        this.id = requestID;
        this.params = Arrays.asList(new SigninParam(username, password));
    }
}
