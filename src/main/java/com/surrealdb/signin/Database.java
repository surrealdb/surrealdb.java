package com.surrealdb.signin;

/**
 * The Database class represents a specific level of credentials for signing into a SurrealDB database.
 * This class extends the Namespace class and adds a specific database reference, further scoping the sign-in process.
 * <p>
 * The credentials include username, password, namespace, and a specific database.
 */
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
