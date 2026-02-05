package com.surrealdb.signin;

/**
 * Base type for all authentication credentials used with {@link com.surrealdb.Surreal#signin(Credential)}.
 * Implementations: {@link Root}, {@link Namespace}, {@link Database}, {@link Record}, {@link Bearer}.
 */
public interface Credential {
}
