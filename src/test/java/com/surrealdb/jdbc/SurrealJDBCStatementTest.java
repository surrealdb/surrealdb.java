package com.surrealdb.jdbc;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled("Disabled until implementation started")
class SurrealJDBCStatementTest {

    private final Statement statement = new SurrealJDBCStatement();

    @Test
    void executeQuery() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.executeQuery(""));
    }

    @Test
    void executeUpdate() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.executeUpdate(""));
    }

    @Test
    void close() {
        assertThrows(UnsupportedOperationException.class, this.statement::close);
    }

    @Test
    void getMaxFieldSize() {
        assertThrows(UnsupportedOperationException.class, this.statement::getMaxFieldSize);
    }

    @Test
    void setMaxFieldSize() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.setMaxFieldSize(0));
    }

    @Test
    void getMaxRows() {
        assertThrows(UnsupportedOperationException.class, this.statement::getMaxRows);
    }

    @Test
    void setMaxRows() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.setMaxRows(0));
    }

    @Test
    void setEscapeProcessing() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.statement.setEscapeProcessing(false));
    }

    @Test
    void getQueryTimeout() {
        assertThrows(UnsupportedOperationException.class, this.statement::getQueryTimeout);
    }

    @Test
    void setQueryTimeout() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.setQueryTimeout(0));
    }

    @Test
    void cancel() {
        assertThrows(UnsupportedOperationException.class, this.statement::cancel);
    }

    @Test
    void getWarnings() {
        assertThrows(UnsupportedOperationException.class, this.statement::getWarnings);
    }

    @Test
    void clearWarnings() {
        assertThrows(UnsupportedOperationException.class, this.statement::clearWarnings);
    }

    @Test
    void setCursorName() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.setCursorName(""));
    }

    @Test
    void execute() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.execute(""));
    }

    @Test
    void getResultSet() {
        assertThrows(UnsupportedOperationException.class, this.statement::getResultSet);
    }

    @Test
    void getUpdateCount() {
        assertThrows(UnsupportedOperationException.class, this.statement::getUpdateCount);
    }

    @Test
    void getMoreResults() {
        assertThrows(UnsupportedOperationException.class, this.statement::getMoreResults);
    }

    @Test
    void setFetchDirection() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.statement.setFetchDirection(ResultSet.FETCH_FORWARD));
    }

    @Test
    void getFetchDirection() {
        assertThrows(UnsupportedOperationException.class, this.statement::getFetchDirection);
    }

    @Test
    void setFetchSize() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.setFetchSize(0));
    }

    @Test
    void getFetchSize() {
        assertThrows(UnsupportedOperationException.class, this.statement::getFetchSize);
    }

    @Test
    void getResultSetConcurrency() {
        assertThrows(UnsupportedOperationException.class, this.statement::getResultSetConcurrency);
    }

    @Test
    void getResultSetType() {
        assertThrows(UnsupportedOperationException.class, this.statement::getResultSetType);
    }

    @Test
    void addBatch() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.addBatch(""));
    }

    @Test
    void clearBatch() {
        assertThrows(UnsupportedOperationException.class, this.statement::clearBatch);
    }

    @Test
    void executeBatch() {
        assertThrows(UnsupportedOperationException.class, this.statement::executeBatch);
    }

    @Test
    void getConnection() {
        assertThrows(UnsupportedOperationException.class, this.statement::getConnection);
    }

    @Test
    void testGetMoreResults() {
        assertThrows(UnsupportedOperationException.class, this.statement::getMoreResults);
    }

    @Test
    void getGeneratedKeys() {
        assertThrows(UnsupportedOperationException.class, this.statement::getGeneratedKeys);
    }

    @Test
    void testExecuteUpdate() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.executeUpdate(""));
    }

    @Test
    void testExecuteUpdate1() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.statement.executeUpdate("", Statement.RETURN_GENERATED_KEYS));
    }

    @Test
    void testExecuteUpdate2() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.statement.executeUpdate("", new String[] {}));
    }

    @Test
    void testExecute() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.execute(""));
    }

    @Test
    void testExecute1() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.statement.execute("", new int[10]));
    }

    @Test
    void testExecute2() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.statement.execute("", Statement.RETURN_GENERATED_KEYS));
    }

    @Test
    void getResultSetHoldability() {
        assertThrows(UnsupportedOperationException.class, this.statement::getResultSetHoldability);
    }

    @Test
    void isClosed() {
        assertThrows(UnsupportedOperationException.class, this.statement::isClosed);
    }

    @Test
    void setPoolable() {
        assertThrows(UnsupportedOperationException.class, () -> this.statement.setPoolable(false));
    }

    @Test
    void isPoolable() {
        assertThrows(UnsupportedOperationException.class, this.statement::isPoolable);
    }

    @Test
    void closeOnCompletion() {
        assertThrows(UnsupportedOperationException.class, this.statement::closeOnCompletion);
    }

    @Test
    void isCloseOnCompletion() {
        assertThrows(UnsupportedOperationException.class, this.statement::isCloseOnCompletion);
    }

    @Test
    void unwrap() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.statement.unwrap(String.class));
    }

    @Test
    void isWrapperFor() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.statement.isWrapperFor(String.class));
    }
}
