package com.surrealdb.refactor.driver;

import java.util.List;
import lombok.Getter;

@Getter
public class SigninMessage {
    private final String method = "signin";
    private final String id;

    private final List<SigninParam> params;

    public SigninMessage(final String requestID, final String username, final String password) {
        this.id = requestID;
        this.params = List.of(new SigninParam(username, password));
    }
}
