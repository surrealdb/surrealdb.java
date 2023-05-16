package com.surrealdb.jdbc;

import com.surrealdb.jdbc.exception.SurrealJDBCDriverInitializationException;
import java.net.URI;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class SurrealJDBCDriver implements Driver {
    private static final Driver INSTANCE = new SurrealJDBCDriver();
    private static boolean registered;

    public SurrealJDBCDriver() {
        // Default constructor
    }

    @Override
    public SurrealJDBCConnection connect(String url, Properties info)
            throws SQLException, SurrealJDBCDriverInitializationException {
        /*
            Note:
            I think it would be wise to create a method to parse the information for tls, async, dbName
            from the url and not from the Properties object, please give feedback about this. :)

            URL suggestion: jdbc:surrealdb://host:port/dbname;namespace;optionalParam2=SomeValue2;
        */
        SurrealJDBCConnection connection;
        String host, user, password, dbName, namespace;
        boolean useTls, useAsync;
        int port;

        if (!url.contains("jdbc")) {
            throw new SurrealJDBCDriverInitializationException("Missing 'jdbc' in URI!");
        }

        URI uri = URI.create(url.replace("jdbc:", ""));

        if (!uri.getScheme().equals("surrealdb")) {
            throw new SurrealJDBCDriverInitializationException(
                    "Failed to validate the JDBC-URL: Missing scheme 'surrealdb'");
        }

        host = uri.getHost();
        port = uri.getPort();
        dbName = uri.getPath().split(";")[0];
        namespace = uri.getPath().split(";")[1];

        user = info.getProperty("user");
        password = info.getProperty("password");
        useTls = info.getProperty("tls").equals("true");
        useAsync = info.getProperty("async").equals("true");

        return new SurrealJDBCConnection(
                host, port, dbName, namespace, user, password, useTls, useAsync);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMinorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean jdbcCompliant() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException();
    }
}
