package com.surrealdb.signin;

/**
 * Marker interface for sign-in credentials (Root, Namespace, Database).
 * Extends {@link Credential} for backward compatibility.
 *
 * @deprecated Use {@link Credential} and {@link com.surrealdb.Surreal#signin(Credential)} instead.
 */
@Deprecated
public interface Signin extends Credential {
}
