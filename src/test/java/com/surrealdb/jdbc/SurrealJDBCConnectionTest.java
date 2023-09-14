package com.surrealdb.jdbc;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled("Disabled until implementation started")
class SurrealJDBCConnectionTest {

    private final SurrealJDBCConnection jdbcConnection = new SurrealJDBCConnection();

    @SneakyThrows
    @Test
    void createStatement() {
        assertInstanceOf(Statement.class, this.jdbcConnection.createStatement());
    }

    @SneakyThrows
    @Test
    void prepareStatement() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareStatement("SELECT * FROM "));
    }

    @Test
    void prepareCall() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareCall("SELECT * FROM "));
    }

    @Test
    void nativeSQL() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.nativeSQL("SELECT * FROM "));
    }

    @Test
    void setAutoCommit() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.setAutoCommit(false));
    }

    @Test
    void getAutoCommit() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getAutoCommit());
    }

    @Test
    void commit() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.commit());
    }

    @Test
    void rollback() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.rollback());
    }

    @Test
    void close() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.close());
    }

    @Test
    void isClosed() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.isClosed());
    }

    @Test
    void getMetaData() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getMetaData());
    }

    @Test
    void setReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.setReadOnly(false));
    }

    @Test
    void isReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.isReadOnly());
    }

    @Test
    void setCatalog() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.setCatalog("a catalogue"));
    }

    @Test
    void getCatalog() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getCatalog());
    }

    @Test
    void setTransactionIsolation() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.setTransactionIsolation(0));
    }

    @Test
    void getTransactionIsolation() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.getTransactionIsolation());
    }

    @Test
    void getWarnings() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getWarnings());
    }

    @Test
    void clearWarnings() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.close());
    }

    @Test
    void testCreateStatement() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.createStatement(0, 0));
    }

    @Test
    void testPrepareStatement() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareStatement("SELECT * FROM ", 0, 1));
    }

    @Test
    void testPrepareCall() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareCall("SELECT * FROM ", 0, 1));
    }

    @Test
    void getTypeMap() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getTypeMap());
    }

    @Test
    void setTypeMap() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.setTypeMap(null));
    }

    @Test
    void setHoldability() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.setHoldability(0));
    }

    @Test
    void getHoldability() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getHoldability());
    }

    @Test
    void setSavepoint() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.setSavepoint());
    }

    @Test
    void testSetSavepoint() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.setSavepoint("save"));
    }

    @Test
    void testRollback() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.rollback(null));
    }

    @Test
    void releaseSavepoint() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.releaseSavepoint(null));
    }

    @Test
    void testCreateStatement1() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.createStatement(0, 0, 0));
    }

    @Test
    void testPrepareStatement1() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareStatement(null, 0, 0, 0));
    }

    @Test
    void testPrepareCall1() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.prepareCall(null, 0, 0));
    }

    @Test
    void testPrepareStatement2() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareStatement(null, 0));
    }

    @Test
    void testPrepareStatement3() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareStatement(null, new int[] {10, 20, 30, 40}));
    }

    @Test
    void testPrepareStatement4() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.prepareStatement(null, new String[] {}));
    }

    @Test
    void createClob() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.createClob());
    }

    @Test
    void createBlob() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.createBlob());
    }

    @Test
    void createNClob() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.createNClob());
    }

    @Test
    void createSQLXML() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.createSQLXML());
    }

    @Test
    void isValid() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.isValid(0));
    }

    @Test
    void setClientInfo() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.setClientInfo("", ""));
    }

    @Test
    void testSetClientInfo() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.setClientInfo(null));
    }

    @Test
    void getClientInfo() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getClientInfo());
    }

    @Test
    void testGetClientInfo() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getClientInfo(""));
    }

    @Test
    void createArrayOf() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.createArrayOf(null, null));
    }

    @Test
    void createStruct() {
        assertThrows(
                UnsupportedOperationException.class, () -> this.jdbcConnection.createStruct(null, null));
    }

    @Test
    void setSchema() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.setSchema(null));
    }

    @Test
    void getSchema() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getSchema());
    }

    @Test
    void abort() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    this.jdbcConnection.abort(null);
                });
    }

    @Test
    void setNetworkTimeout() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> this.jdbcConnection.setNetworkTimeout(null, 0));
    }

    @Test
    void getNetworkTimeout() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.getNetworkTimeout());
    }

    @Test
    void unwrap() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.unwrap(null));
    }

    @Test
    void isWrapperFor() {
        assertThrows(UnsupportedOperationException.class, () -> this.jdbcConnection.isWrapperFor(null));
    }
}
