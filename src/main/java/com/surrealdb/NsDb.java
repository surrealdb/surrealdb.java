package com.surrealdb;

/**
 * Holds the current namespace and database after a {@link Surreal#useNs(String)},
 * {@link Surreal#useDb(String)}, or {@link Surreal#useDefaults()} call.
 * Returned by those methods so the client can use the actual ns/db set by the server.
 */
public class NsDb {

    private final String namespace;
    private final String database;

    /**
     * Creates an NsDb with the given namespace and database (both may be null).
     *
     * @param namespace the current namespace, or null
     * @param database  the current database, or null
     */
    public NsDb(String namespace, String database) {
        this.namespace = namespace;
        this.database = database;
    }

    /**
     * Returns the current namespace.
     *
     * @return the namespace, or null
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the current database.
     *
     * @return the database, or null
     */
    public String getDatabase() {
        return database;
    }
}
