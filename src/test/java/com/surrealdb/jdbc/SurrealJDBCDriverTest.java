package com.surrealdb.jdbc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Driver;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled("Disabled until implementation started")
class SurrealJDBCDriverTest {
    private final Driver driver = new SurrealJDBCDriver();

    @Test
    void connect() {
        assertThrows(UnsupportedOperationException.class, () -> this.driver.connect("", null));
    }

    @Test
    void acceptsURL() {
        assertThrows(UnsupportedOperationException.class, () -> this.driver.acceptsURL(""));
    }

    @Test
    void getPropertyInfo() {
        assertThrows(UnsupportedOperationException.class, () -> this.driver.getPropertyInfo("", null));
    }

    @Test
    void getMajorVersion() {
        assertThrows(UnsupportedOperationException.class, this.driver::getMajorVersion);
    }

    @Test
    void getMinorVersion() {
        assertThrows(UnsupportedOperationException.class, this.driver::getMinorVersion);
    }

    @Test
    void jdbcCompliant() {
        assertThrows(UnsupportedOperationException.class, this.driver::jdbcCompliant);
    }

    @Test
    void getParentLogger() {
        assertThrows(UnsupportedOperationException.class, this.driver::getParentLogger);
    }
}
