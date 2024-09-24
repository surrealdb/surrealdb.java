package com.surrealdb.signin;

public class Database extends Namespace implements Signin {

    private final String database;

    public Database(String username, String password, String namespace, String database) {
        super(username, password, namespace);
        this.database = database;
    }


    public String getDatabase() {
        return database;
    }

}
