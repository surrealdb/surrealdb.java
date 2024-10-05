package com.surrealdb.signin;

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
