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
public class SurrealDatabaseCredentials implements SurrealAuthCredentials {

    String user;
    String pass;
    String NS;
    String DB;

    public static SurrealDatabaseCredentials from(String user, String password, String namespace, String database) {
        return new SurrealDatabaseCredentials(user, password, namespace, database);
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

    public String getDatabase() {
        return DB;
    }
}
