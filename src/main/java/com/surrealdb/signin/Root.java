package com.surrealdb.signin;

/**
 * The Root class represents a level of credentials used for signing into the SurrealDB database.
 * This class implements the Signin interface, providing the basic username and password authentication.
 */
public class Root implements Signin {

    private final String username;
    private final String password;

    public Root(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
