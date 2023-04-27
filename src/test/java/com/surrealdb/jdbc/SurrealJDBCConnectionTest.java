package com.surrealdb.jdbc;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Statement;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Disabled until implementation started")
class SurrealJDBCConnectionTest {

    private SurrealJDBCConnection jdbcConnection = new SurrealJDBCConnection(TestUtils.getHost(), TestUtils.getPort(), TestUtils.getDatabase(), TestUtils.getUseTlsDriver(), TestUtils.getUseAsyncDriver());

    @SneakyThrows
    @Test
    void createStatement() {
        assertInstanceOf(Statement.class, jdbcConnection.createStatement());
    }

    @SneakyThrows
    @Test
    void prepareStatement() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareStatement("SELECT * FROM "));
    }

    @Test
    void prepareCall() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareCall("SELECT * FROM "));
    }

    @Test
    void nativeSQL() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.nativeSQL("SELECT * FROM "));
    }

    @Test
    void setAutoCommit() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.setAutoCommit(false));
    }

    @Test
    void getAutoCommit() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getAutoCommit());
    }

    @Test
    void commit() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.commit());
    }

    @Test
    void rollback() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.rollback());
    }

    @Test
    void close() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.close());
    }

    @Test
    void isClosed() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.isClosed());
    }

    @Test
    void getMetaData() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getMetaData());
    }

    @Test
    void setReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.setReadOnly(false));
    }

    @Test
    void isReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.isReadOnly());
    }

    @Test
    void setCatalog() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.setCatalog("a catalogue"));
    }

    @Test
    void getCatalog() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getCatalog());
    }

    @Test
    void setTransactionIsolation() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.setTransactionIsolation(0));
    }

    @Test
    void getTransactionIsolation() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.getTransactionIsolation());
    }

    @Test
    void getWarnings() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getWarnings());
    }

    @Test
    void clearWarnings() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.close());
    }

    @Test
    void testCreateStatement() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.createStatement(0, 0));
    }

    @Test
    void testPrepareStatement() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareStatement("SELECT * FROM ", 0, 1));
    }

    @Test
    void testPrepareCall() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareCall("SELECT * FROM ", 0, 1));
    }

    @Test
    void getTypeMap() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getTypeMap());
    }

    @Test
    void setTypeMap() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.setTypeMap(null));
    }

    @Test
    void setHoldability() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.setHoldability(0));
    }

    @Test
    void getHoldability() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getHoldability());
    }

    @Test
    void setSavepoint() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.setSavepoint());
    }

    @Test
    void testSetSavepoint() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.setSavepoint("save"));
    }

    @Test
    void testRollback() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.rollback(null));
    }

    @Test
    void releaseSavepoint() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.releaseSavepoint(null));
    }

    @Test
    void testCreateStatement1() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.createStatement(0, 0, 0));
    }

    @Test
    void testPrepareStatement1() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareStatement(null, 0, 0, 0));
    }

    @Test
    void testPrepareCall1() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.prepareCall(null, 0, 0));
    }

    @Test
    void testPrepareStatement2() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareStatement(null, 0));
    }

    @Test
    void testPrepareStatement3() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareStatement(null, new int[] {10, 20, 30, 40}));
    }

    @Test
    void testPrepareStatement4() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.prepareStatement(null, new String[] {}));
    }

    @Test
    void createClob() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.createClob());
    }

    @Test
    void createBlob() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.createBlob());
    }

    @Test
    void createNClob() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.createNClob());
    }

    @Test
    void createSQLXML() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.createSQLXML());
    }

    @Test
    void isValid() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.isValid(0));
    }

    @Test
    void setClientInfo() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.setClientInfo("", ""));
    }

    @Test
    void testSetClientInfo() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.setClientInfo(null));
    }

    @Test
    void getClientInfo() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getClientInfo());
    }

    @Test
    void testGetClientInfo() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getClientInfo(""));
    }

    @Test
    void createArrayOf() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.createArrayOf(null, null));
    }

    @Test
    void createStruct() {
        assertThrows(
                UnsupportedOperationException.class, () -> jdbcConnection.createStruct(null, null));
    }

    @Test
    void setSchema() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.setSchema(null));
    }

    @Test
    void getSchema() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getSchema());
    }

    @Test
    void abort() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> {
                    jdbcConnection.abort(null);
                });
    }

    @Test
    void setNetworkTimeout() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> jdbcConnection.setNetworkTimeout(null, 0));
    }

    @Test
    void getNetworkTimeout() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.getNetworkTimeout());
    }

    @Test
    void unwrap() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.unwrap(null));
    }

    @Test
    void isWrapperFor() {
        assertThrows(UnsupportedOperationException.class, () -> jdbcConnection.isWrapperFor(null));
    }
}
