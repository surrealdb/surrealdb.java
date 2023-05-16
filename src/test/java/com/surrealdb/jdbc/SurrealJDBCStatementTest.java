package com.surrealdb.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.jdbc.model.Person;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.*;

class SurrealJDBCStatementTest {

    private Statement statement;

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
        Assert.assertTrue(TestUtils.getHost() != null);

        var uri = URI.create(URL);

        // Initialize the JDBC Driver
        Class.forName("com.surrealdb.jdbc.SurrealJDBCDriver");
        DriverManager.registerDriver(new SurrealJDBCDriver());
        jdbcConnection = (SurrealJDBCConnection) DriverManager.getConnection(URL, TestUtils.getDriverProperties());

        statement = jdbcConnection.createStatement();

        // Let's create two objects for testing with the driver
        try {
            var created = statement.executeQuery("""
CREATE person:1 CONTENT {
    id: 'person:1',
    marketing: true,
    name: {
        first: 'Flip',
        last: 'Flopsen'
    },
    title: 'NiceDude'
    };
CREATE person:2 CONTENT {
    id: 'person:2',
    marketing: true,
    name: {
        first: 'Hugh',
        last: 'G'
    },
    title: 'Polbrit'
    };
""");
        } catch (SQLException e) {
            System.err.println("Failed to create person!");
        }
    }

    @AfterEach
    public void teardown() {
        try {
            statement.executeQuery("DELETE person");
        } catch (SQLException e) {
            System.err.println("Failed to delete person!");
        }
    }

    @Test
    void executeQuery() {
        try {
            var results = statement.executeQuery("select * from person");

            var ctr = 0;
            while (results.next()) {
                var obj = results.getObject(ctr, Person.class);
                System.out.println(obj.getName());
                Assert.assertTrue(obj != null);
                ctr++;
            }

            Assert.assertTrue(ctr == 2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void executeQueryWithArgs() {
        try {
            var results = statement.executeQuery("select * from person where name.first = $firstName withargs [firstName, Flip]");

            var ctr = 0;
            while (results.next()) {
                var obj = results.getObject(ctr, Person.class);
                Assert.assertTrue(obj != null);
                ctr++;
            }

            Assert.assertTrue(ctr == 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    void executeUpdate() {
        try {
            statement.executeUpdate("UPDATE person SET marketing = false");

            var results = statement.executeQuery("select * from person");

            var ctr = 0;
            while (results.next()) {
                var obj = results.getObject(ctr, Person.class);
                Assert.assertFalse(obj.isMarketing());
                ctr++;
            }

            Assert.assertTrue(ctr == 2);
            ctr = 0;
            statement.executeUpdate("UPDATE person:1 SET marketing = true");

            results = statement.executeQuery("select * from person where marketing = false");

            while (results.next()) {
                var obj = results.getObject(ctr, Person.class);
                Assert.assertFalse(obj.isMarketing());
                ctr++;
            }

            Assert.assertTrue(ctr == 1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Disabled
    void close() {
        assertThrows(UnsupportedOperationException.class, statement::close);
    }

    @Disabled
    void getMaxFieldSize() {
        assertThrows(UnsupportedOperationException.class, statement::getMaxFieldSize);
    }

    @Disabled
    void setMaxFieldSize() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setMaxFieldSize(0));
    }

    @Disabled
    void getMaxRows() {
        assertThrows(UnsupportedOperationException.class, statement::getMaxRows);
    }

    @Disabled
    void setMaxRows() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setMaxRows(0));
    }

    @Disabled
    void setEscapeProcessing() {
        assertThrows(
                UnsupportedOperationException.class, () -> statement.setEscapeProcessing(false));
    }

    @Disabled
    void getQueryTimeout() {
        assertThrows(UnsupportedOperationException.class, statement::getQueryTimeout);
    }

    @Disabled
    void setQueryTimeout() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setQueryTimeout(0));
    }

    @Disabled
    void cancel() {
        assertThrows(UnsupportedOperationException.class, statement::cancel);
    }

    @Disabled
    void getWarnings() {
        assertThrows(UnsupportedOperationException.class, statement::getWarnings);
    }

    @Disabled
    void clearWarnings() {
        assertThrows(UnsupportedOperationException.class, statement::clearWarnings);
    }

    @Disabled
    void setCursorName() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setCursorName(""));
    }

    @Disabled
    void execute() {
        assertThrows(UnsupportedOperationException.class, () -> statement.execute(""));
    }

    @Disabled
    void getResultSet() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSet);
    }

    @Disabled
    void getUpdateCount() {
        assertThrows(UnsupportedOperationException.class, statement::getUpdateCount);
    }

    @Disabled
    void getMoreResults() {
        assertThrows(UnsupportedOperationException.class, statement::getMoreResults);
    }

    @Disabled
    void setFetchDirection() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> statement.setFetchDirection(ResultSet.FETCH_FORWARD));
    }

    @Disabled
    void getFetchDirection() {
        assertThrows(UnsupportedOperationException.class, statement::getFetchDirection);
    }

    @Disabled
    void setFetchSize() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setFetchSize(0));
    }

    @Disabled
    void getFetchSize() {
        assertThrows(UnsupportedOperationException.class, statement::getFetchSize);
    }

    @Disabled
    void getResultSetConcurrency() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSetConcurrency);
    }

    @Disabled
    void getResultSetType() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSetType);
    }

    @Disabled
    void addBatch() {
        assertThrows(UnsupportedOperationException.class, () -> statement.addBatch(""));
    }

    @Disabled
    void clearBatch() {
        assertThrows(UnsupportedOperationException.class, statement::clearBatch);
    }

    @Disabled
    void executeBatch() {
        assertThrows(UnsupportedOperationException.class, statement::executeBatch);
    }

    @Disabled
    void getConnection() {
        assertThrows(UnsupportedOperationException.class, statement::getConnection);
    }

    @Disabled
    void testGetMoreResults() {
        assertThrows(UnsupportedOperationException.class, statement::getMoreResults);
    }

    @Disabled
    void getGeneratedKeys() {
        assertThrows(UnsupportedOperationException.class, statement::getGeneratedKeys);
    }

    @Disabled
    void testExecuteUpdate() {
        assertThrows(UnsupportedOperationException.class, () -> statement.executeUpdate(""));
    }

    @Disabled
    void testExecuteUpdate1() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> statement.executeUpdate("", Statement.RETURN_GENERATED_KEYS));
    }

    @Disabled
    void testExecuteUpdate2() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> statement.executeUpdate("", new String[] {}));
    }

    @Disabled
    void testExecute() {
        assertThrows(UnsupportedOperationException.class, () -> statement.execute(""));
    }

    @Disabled
    void testExecute1() {
        assertThrows(UnsupportedOperationException.class, () -> statement.execute("", new int[10]));
    }

    @Disabled
    void testExecute2() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> statement.execute("", Statement.RETURN_GENERATED_KEYS));
    }

    @Disabled
    void getResultSetHoldability() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSetHoldability);
    }

    @Disabled
    void isClosed() {
        assertThrows(UnsupportedOperationException.class, statement::isClosed);
    }

    @Disabled
    void setPoolable() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setPoolable(false));
    }

    @Disabled
    void isPoolable() {
        assertThrows(UnsupportedOperationException.class, statement::isPoolable);
    }

    @Disabled
    void closeOnCompletion() {
        assertThrows(UnsupportedOperationException.class, statement::closeOnCompletion);
    }

    @Disabled
    void isCloseOnCompletion() {
        assertThrows(UnsupportedOperationException.class, statement::isCloseOnCompletion);
    }

    @Disabled
    void unwrap() {
        assertThrows(UnsupportedOperationException.class, () -> statement.unwrap(String.class));
    }

    @Disabled
    void isWrapperFor() {
        assertThrows(
                UnsupportedOperationException.class, () -> statement.isWrapperFor(String.class));
    }
}
