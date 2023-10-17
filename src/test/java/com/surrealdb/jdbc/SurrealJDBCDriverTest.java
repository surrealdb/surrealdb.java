package com.surrealdb.jdbc;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.jdbc.model.Person;

import java.net.URI;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Disabled until implementation started")
class SurrealJDBCDriverTest {
    private final Driver driver = new SurrealJDBCDriver();
    private SyncSurrealDriver surrealDriver;
    private SurrealJDBCConnection jdbcConnection;

    private final static String URL = "jdbc:surrealdb://"
        + TestUtils.getHost()
        + ":"
        + TestUtils.getPort()
        + "/"
        + TestUtils.getDatabase()
        + ";"
        + TestUtils.getNamespace();

    @BeforeEach
    public void setup() throws ClassNotFoundException, SQLException {
        var uri = URI.create(URL);

        // Initialize SurrealDB
        SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(
                        TestUtils.getHost(), TestUtils.getPort(), false);
        connection.connect(5);

        surrealDriver = new SyncSurrealDriver(connection);

        surrealDriver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        surrealDriver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        surrealDriver.create(
                "person:1", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        surrealDriver.create(
                "person:2", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));

        // Initialize the JDBC Driver
        Class.forName("com.surrealdb.jdbc.SurrealJDBCDriver");
        DriverManager.registerDriver(new SurrealJDBCDriver());
        jdbcConnection = (SurrealJDBCConnection) DriverManager.getConnection(URL, TestUtils.getDriverProperties());
    }

    @AfterEach
    public void teardown() {
        surrealDriver.delete("person:1");
        surrealDriver.delete("person:2");
    }

    @Test
    void connect() throws SQLException {
        driver.connect(URL, TestUtils.getDriverProperties());
        Assert.assertTrue(TestUtils.getHost().equals(jdbcConnection.getSchema()));
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
        assertThrows(UnsupportedOperationException.class, driver::getMajorVersion);
    }

    @Test
    void getMinorVersion() {
        assertThrows(UnsupportedOperationException.class, driver::getMinorVersion);
    }

    @Test
    void jdbcCompliant() {
        assertThrows(UnsupportedOperationException.class, driver::jdbcCompliant);
    }

    @Test
    void getParentLogger() {
        assertThrows(UnsupportedOperationException.class, driver::getParentLogger);
    }
}
