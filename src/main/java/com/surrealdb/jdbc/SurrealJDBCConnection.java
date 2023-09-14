package com.surrealdb.jdbc;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class SurrealJDBCConnection implements Connection {
    @Override
    public Statement createStatement() {
        return new SurrealJDBCStatement();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql) {
        throw new UnsupportedOperationException("prepared statements are currently unimplemented");
    }

    @Override
    public CallableStatement prepareCall(final String sql) {
        throw new UnsupportedOperationException("callable statements are currently unimplemented");
    }

    @Override
    public String nativeSQL(final String sql) {
        throw new UnsupportedOperationException("native sql is currently unimplemented");
    }

    @Override
    public boolean getAutoCommit() {
        throw new UnsupportedOperationException("autocommit is currently unsupported");
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) {
        throw new UnsupportedOperationException("autocommit is currently unsupported");
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException("commit is currently unsupported");
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException("rollback is currently unsupported");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("connection close is currently unsupported");
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException("connection isclosed is currently unsupported");
    }

    @Override
    public DatabaseMetaData getMetaData() {
        throw new UnsupportedOperationException("connection getMetaData is currently unsupported");
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("connection isReadOnly is unimplemented");
    }

    @Override
    public void setReadOnly(final boolean readOnly) {
        throw new UnsupportedOperationException("connection setReadOnly is currently unsupported");
    }

    @Override
    public String getCatalog() {
        throw new UnsupportedOperationException("connection getCatalog is unimplemented");
    }

    @Override
    public void setCatalog(final String catalog) {
        throw new UnsupportedOperationException("connection setCatalog is unimplemented");
    }

    @Override
    public int getTransactionIsolation() {
        throw new UnsupportedOperationException(
                "connection getTransactionIsolation is unimplemented");
    }

    @Override
    public void setTransactionIsolation(final int level) {
        throw new UnsupportedOperationException(
                "connection setTransactionIsolation is unimplemented");
    }

    @Override
    public SQLWarning getWarnings() {
        throw new UnsupportedOperationException("connection getWarnings is unimplemented");
    }

    @Override
    public void clearWarnings() {
        throw new UnsupportedOperationException("connection clearWarningns is unimplemented");
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) {
        throw new UnsupportedOperationException("connection createStatement is unimplemented");
    }

    @Override
    public PreparedStatement prepareStatement(
            final String sql, final int resultSetType, final int resultSetConcurrency) {
        throw new UnsupportedOperationException("connection prepareStatement is unimplemented");
    }

    @Override
    public CallableStatement prepareCall(
            final String sql, final int resultSetType, final int resultSetConcurrency) {
        throw new UnsupportedOperationException("connection prepareCall is unimplemneted");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() {
        throw new UnsupportedOperationException("connection getTypeMap is unimplemented");
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) {
        throw new UnsupportedOperationException("connection setTypeMap is unimplemented");
    }

    @Override
    public int getHoldability() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHoldability(final int holdability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Savepoint setSavepoint(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback(final Savepoint savepoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement createStatement(
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(
            final String sql,
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CallableStatement prepareCall(
            final String sql,
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob createClob() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob createBlob() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob createNClob() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML createSQLXML() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid(final int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(final String name, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getClientInfo(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getClientInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClientInfo(final Properties properties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSchema() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSchema(final String schema) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abort(final Executor executor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNetworkTimeout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        throw new UnsupportedOperationException();
    }
}
