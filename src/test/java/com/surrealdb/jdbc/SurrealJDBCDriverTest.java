package com.surrealdb.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.Driver;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SurrealJDBCDriverTest {
    private Driver driver = new SurrealJDBCDriver();

    @Test
    void connect() {
        assertThrows(UnsupportedOperationException.class, () -> driver.connect("", null));
    }

    @Test
    void acceptsURL() {
        assertThrows(UnsupportedOperationException.class, () -> driver.acceptsURL(""));
    }

    @Test
    void getPropertyInfo() {
        assertThrows(UnsupportedOperationException.class, () -> driver.getPropertyInfo("", null));

    }

    @Test
    void getMajorVersion() {
        assertThrows(UnsupportedOperationException.class, () -> driver.getMajorVersion());

    }

    @Test
    void getMinorVersion() {
        assertThrows(UnsupportedOperationException.class, () -> driver.getMinorVersion());

    }

    @Test
    void jdbcCompliant() {
        assertThrows(UnsupportedOperationException.class, () -> driver.jdbcCompliant());

    }

    @Test
    void getParentLogger() {
        assertThrows(UnsupportedOperationException.class, () -> driver.getParentLogger());

    }
}
