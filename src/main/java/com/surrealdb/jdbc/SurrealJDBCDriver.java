package com.surrealdb.jdbc;

import com.surrealdb.jdbc.exception.SurrealJDBCDriverInitializationException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public class SurrealJDBCDriver implements Driver {
    @Override
    public SurrealJDBCConnection connect(String url, Properties info) throws SQLException {
        /*
            Note:
            I think it would be wise to create a method to parse the information for tls, async, dbName
            from the url and not from the Properties object, please give feedback about this. :)
        */
        SurrealJDBCConnection connection;
        String host, user, password, dbName;
        boolean useTls, useAsync;
        int port;
        user = info.getProperty("user");
        password = info.getProperty("password");
        dbName = info.getProperty("dbName");
        useTls = info.getProperty("tls").equals("true");
        useAsync = info.getProperty("async").equals("true");

        // Maybe an infancy way of doing it
        var hostAndPort = url.split("//")[1];

        host = hostAndPort.split(":")[0];

        try {
            port = Integer.parseInt(hostAndPort.split(":")[1].split("/")[0]);
        } catch (NumberFormatException e) {
            throw new SurrealJDBCDriverInitializationException("The JDBC Connection URL seems malformed.");
        }

        connection = new SurrealJDBCConnection(host, port, dbName, useTls, useAsync);



        throw new UnsupportedOperationException();
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
