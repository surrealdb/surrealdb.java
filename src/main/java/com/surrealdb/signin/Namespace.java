package com.surrealdb.signin;

/**
 * The Namespace class represents a specific level of credentials for signing into a SurrealDB namespace.
 * This class extends the Root class and adds a namespace reference, further scoping the sign-in process.
 * <p>
 * The credentials include username, password, and a namespace.
 */
public class Namespace extends Root implements Signin {

    private final String namespace;

    public Namespace(String username, String password, String namespace) {
        super(username, password);
        this.namespace = namespace;
    }


    public String getNamespace() {
        return namespace;
    }

}
