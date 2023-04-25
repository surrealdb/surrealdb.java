package com.surrealdb.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class SurrealJDBCStatementTest {

    private final Statement statement = new SurrealJDBCStatement();

    @Test
    void executeQuery() {
        assertThrows(UnsupportedOperationException.class, () -> statement.executeQuery(""));
    }

    @Test
    void executeUpdate() {
        assertThrows(UnsupportedOperationException.class, () -> statement.executeUpdate(""));
    }

    @Test
    void close() {
        assertThrows(UnsupportedOperationException.class, statement::close);
    }

    @Test
    void getMaxFieldSize() {
        assertThrows(UnsupportedOperationException.class, statement::getMaxFieldSize);
    }

    @Test
    void setMaxFieldSize() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setMaxFieldSize(0));
    }

    @Test
    void getMaxRows() {
        assertThrows(UnsupportedOperationException.class, statement::getMaxRows);
    }

    @Test
    void setMaxRows() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setMaxRows(0));
    }

    @Test
    void setEscapeProcessing() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setEscapeProcessing(false));
    }

    @Test
    void getQueryTimeout() {
        assertThrows(UnsupportedOperationException.class, statement::getQueryTimeout);
    }

    @Test
    void setQueryTimeout() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setQueryTimeout(0));
    }

    @Test
    void cancel() {
        assertThrows(UnsupportedOperationException.class, statement::cancel);
    }

    @Test
    void getWarnings() {
        assertThrows(UnsupportedOperationException.class, statement::getWarnings);
    }

    @Test
    void clearWarnings() {
        assertThrows(UnsupportedOperationException.class, statement::clearWarnings);
    }

    @Test
    void setCursorName() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setCursorName(""));
    }

    @Test
    void execute() {
        assertThrows(UnsupportedOperationException.class, () -> statement.execute(""));
    }

    @Test
    void getResultSet() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSet);
    }

    @Test
    void getUpdateCount() {
        assertThrows(UnsupportedOperationException.class, statement::getUpdateCount);
    }

    @Test
    void getMoreResults() {
        assertThrows(UnsupportedOperationException.class, statement::getMoreResults);
    }

    @Test
    void setFetchDirection() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setFetchDirection(ResultSet.FETCH_FORWARD));
    }

    @Test
    void getFetchDirection() {
        assertThrows(UnsupportedOperationException.class, statement::getFetchDirection);
    }

    @Test
    void setFetchSize() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setFetchSize(0));
    }

    @Test
    void getFetchSize() {
        assertThrows(UnsupportedOperationException.class, statement::getFetchSize);
    }

    @Test
    void getResultSetConcurrency() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSetConcurrency);
    }

    @Test
    void getResultSetType() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSetType);
    }

    @Test
    void addBatch() {
        assertThrows(UnsupportedOperationException.class, () -> statement.addBatch(""));
    }

    @Test
    void clearBatch() {
        assertThrows(UnsupportedOperationException.class, statement::clearBatch);
    }

    @Test
    void executeBatch() {
        assertThrows(UnsupportedOperationException.class, statement::executeBatch);
    }

    @Test
    void getConnection() {
        assertThrows(UnsupportedOperationException.class, statement::getConnection);
    }

    @Test
    void testGetMoreResults() {
        assertThrows(UnsupportedOperationException.class, statement::getMoreResults);
    }

    @Test
    void getGeneratedKeys() {
        assertThrows(UnsupportedOperationException.class, statement::getGeneratedKeys);
    }

    @Test
    void testExecuteUpdate() {
        assertThrows(UnsupportedOperationException.class, () -> statement.executeUpdate(""));
    }

    @Test
    void testExecuteUpdate1() {
        assertThrows(UnsupportedOperationException.class, () -> statement.executeUpdate("", Statement.RETURN_GENERATED_KEYS));
    }

    @Test
    void testExecuteUpdate2() {
        assertThrows(UnsupportedOperationException.class, () -> statement.executeUpdate("", new String[]{}));
    }

    @Test
    void testExecute() {
        assertThrows(UnsupportedOperationException.class, () -> statement.execute(""));
    }

    @Test
    void testExecute1() {
        assertThrows(UnsupportedOperationException.class, () -> statement.execute("", new int[10]));
    }

    @Test
    void testExecute2() {
        assertThrows(UnsupportedOperationException.class, () -> statement.execute("", Statement.RETURN_GENERATED_KEYS));
    }

    @Test
    void getResultSetHoldability() {
        assertThrows(UnsupportedOperationException.class, statement::getResultSetHoldability);
    }

    @Test
    void isClosed() {
        assertThrows(UnsupportedOperationException.class, statement::isClosed);
    }

    @Test
    void setPoolable() {
        assertThrows(UnsupportedOperationException.class, () -> statement.setPoolable(false));
    }

    @Test
    void isPoolable() {
        assertThrows(UnsupportedOperationException.class, statement::isPoolable);
    }

    @Test
    void closeOnCompletion() {
        assertThrows(UnsupportedOperationException.class, statement::closeOnCompletion);
    }

    @Test
    void isCloseOnCompletion() {
        assertThrows(UnsupportedOperationException.class, statement::isCloseOnCompletion);
    }

    @Test
    void unwrap() {
        assertThrows(UnsupportedOperationException.class, () -> statement.unwrap(String.class));
    }

    @Test
    void isWrapperFor() {
        assertThrows(UnsupportedOperationException.class, () -> statement.isWrapperFor(String.class));
    }
}
