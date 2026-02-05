package com.surrealdb.signin;

/**
 * Namespace-level credentials for signing into a SurrealDB namespace.
 * Extends {@link RootCredential} and adds a namespace reference.
 */
public class NamespaceCredential extends RootCredential implements Signin {

    private final String namespace;

    public NamespaceCredential(String username, String password, String namespace) {
        super(username, password);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
