package com.surrealdb.jdbc;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class SurrealJDBCConnection implements Connection {
    @Override
    public Statement createStatement() throws SQLException {
        return new SurrealJDBCStatement();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        throw new UnsupportedOperationException("prepared statements are currently unimplemented");
    }

    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        throw new UnsupportedOperationException("callable statements are currently unimplemented");
    }

    @Override
    public String nativeSQL(final String sql) throws SQLException {
        throw new UnsupportedOperationException("native sql is currently unimplemented");
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        throw new UnsupportedOperationException("autocommit is currently unsupported");
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        throw new UnsupportedOperationException("autocommit is currently unsupported");
    }

    @Override
    public void commit() throws SQLException {
        throw new UnsupportedOperationException("commit is currently unsupported");
    }

    @Override
    public void rollback() throws SQLException {
        throw new UnsupportedOperationException("rollback is currently unsupported");
    }

    @Override
    public void close() throws SQLException {
        throw new UnsupportedOperationException("connection close is currently unsupported");
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new UnsupportedOperationException("connection isclosed is currently unsupported");
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        throw new UnsupportedOperationException("connection getMetaData is currently unsupported");
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        throw new UnsupportedOperationException("connection isReadOnly is unimplemented");
    }

    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        throw new UnsupportedOperationException("connection setReadOnly is currently unsupported");
    }

    @Override
    public String getCatalog() throws SQLException {
        throw new UnsupportedOperationException("connection getCatalog is unimplemented");
    }

    @Override
    public void setCatalog(final String catalog) throws SQLException {
        throw new UnsupportedOperationException("connection setCatalog is unimplemented");
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        throw new UnsupportedOperationException("connection getTransactionIsolation is unimplemented");
    }

    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        throw new UnsupportedOperationException("connection setTransactionIsolation is unimplemented");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new UnsupportedOperationException("connection getWarnings is unimplemented");
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new UnsupportedOperationException("connection clearWarningns is unimplemented");
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException("connection createStatement is unimplemented");
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException("connection prepareStatement is unimplemented");
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        throw new UnsupportedOperationException("connection prepareCall is unimplemneted");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new UnsupportedOperationException("connection getTypeMap is unimplemented");
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        throw new UnsupportedOperationException("connection setTypeMap is unimplemented");
    }

    @Override
    public int getHoldability() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHoldability(final int holdability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob createClob() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob createBlob() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob createNClob() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid(final int timeout) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClientInfo(final String name) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSchema() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSchema(final String schema) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abort(final Executor executor) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
