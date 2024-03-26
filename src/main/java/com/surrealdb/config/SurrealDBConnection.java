package com.surrealdb.config;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;

import java.io.IOException;
import java.util.Properties;

public class SurrealDBConnection {
    private static SyncSurrealDriver driver = null;
    private static final String PROPERTIES_FILE = "/application.properties";
    private static final String host;
    private static final int port;
    private static final boolean useSsl;
    private static final String nameSpace;
    private static final String database;

    static {
        Properties properties = new Properties();
        try {
            properties.load(SurrealDBConnection.class.getResourceAsStream(PROPERTIES_FILE));
            host = properties.getProperty("surrealdb.host", "localhost");
            port = Integer.parseInt(properties.getProperty("surrealdb.port", "8000"));
            useSsl = Boolean.parseBoolean(properties.getProperty("surrealdb.useSsl", "false"));
            nameSpace = properties.getProperty("surrealdb.nameSpace", "test");
            database = properties.getProperty("surrealdb.database", "test") ;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load surrealdb.properties", e);
        }
    }

    private SurrealDBConnection() {
    }

    public static synchronized SyncSurrealDriver getRepoDriver() {
        if (driver == null) {
            SurrealConnection conn = new SurrealWebSocketConnection(host, port, useSsl);
            conn.connect(5);
            driver = new SyncSurrealDriver(conn);
            driver.use(nameSpace, database);
        }
        return driver;
    }
}
