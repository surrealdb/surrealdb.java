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
public class SurrealScopeCredentials implements SurrealAuthCredentials {

    String NS;
    String DB;
    String SC;

    public static SurrealScopeCredentials from(String namespace, String database, String scope) {
        return new SurrealScopeCredentials(namespace, database, scope);
    }

    public String getNamespace() {
        return NS;
    }

    public String getDatabase() {
        return DB;
    }

    public String getScope() {
        return SC;
    }
}
