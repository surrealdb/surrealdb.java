package com.surrealdb.signin;

/**
 * Base type for all authentication credentials used with {@link com.surrealdb.Surreal#signin(Credential)}.
 * Implementations: {@link RootCredential}, {@link NamespaceCredential}, {@link DatabaseCredential}, {@link RecordCredential}, {@link BearerCredential}.
 */
public interface Credential {
}
