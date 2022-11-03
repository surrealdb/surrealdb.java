package com.surrealdb.driver.auth;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class SurrealRootCredentials implements SurrealAuthCredentials {

    String user;
    String pass;

    public static SurrealRootCredentials from(String user, String password) {
        return new SurrealRootCredentials(user, password);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return pass;
    }
}
