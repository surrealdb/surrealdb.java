package com.surrealdb.signin;

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
