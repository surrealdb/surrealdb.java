package com.surrealdb.driver.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class SurrealNamespaceCredentials implements SurrealAuthCredentials {

    String user;
    String pass;
    String NS;

    public static SurrealNamespaceCredentials from(String user, String password, String namespace) {
        return new SurrealNamespaceCredentials(user, password, namespace);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return pass;
    }

    public String getNamespace() {
        return NS;
    }
}
