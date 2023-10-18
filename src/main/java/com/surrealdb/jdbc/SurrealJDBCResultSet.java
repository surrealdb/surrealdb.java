package com.surrealdb.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class SurrealJDBCResultSet implements ResultSet {
    @Override
    public boolean next() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean wasNull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getAsciiStream(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getUnicodeStream(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getBinaryStream(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBytes(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getAsciiStream(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getUnicodeStream(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getBinaryStream(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLWarning getWarnings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearWarnings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCursorName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResultSetMetaData getMetaData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int findColumn(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBeforeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAfterLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean first() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean last() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean absolute(final int row) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean relative(final int rows) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean previous() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchDirection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchDirection(final int direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFetchSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFetchSize(final int rows) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getConcurrency() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowUpdated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowInserted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean rowDeleted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNull(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBoolean(final int columnIndex, final boolean x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateByte(final int columnIndex, final byte x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateShort(final int columnIndex, final short x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInt(final int columnIndex, final int x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLong(final int columnIndex, final long x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFloat(final int columnIndex, final float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDouble(final int columnIndex, final double x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateString(final int columnIndex, final String x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBytes(final int columnIndex, final byte[] x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDate(final int columnIndex, final Date x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTime(final int columnIndex, final Time x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(final int columnIndex, final Object x, final int scaleOrLength) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(final int columnIndex, final Object x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNull(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBoolean(final String columnLabel, final boolean x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateByte(final String columnLabel, final byte x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateShort(final String columnLabel, final short x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateInt(final String columnLabel, final int x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLong(final String columnLabel, final long x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFloat(final String columnLabel, final float x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDouble(final String columnLabel, final double x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBigDecimal(final String columnLabel, final BigDecimal x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateString(final String columnLabel, final String x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBytes(final String columnLabel, final byte[] x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateDate(final String columnLabel, final Date x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTime(final String columnLabel, final Time x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateTimestamp(final String columnLabel, final Timestamp x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(
            final String columnLabel, final InputStream x, final int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(
            final String columnLabel, final Reader reader, final int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(final String columnLabel, final Object x, final int scaleOrLength) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateObject(final String columnLabel, final Object x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cancelRowUpdates() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToInsertRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void moveToCurrentRow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Statement getStatement() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(final int columnIndex, final Map<String, Class<?>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getObject(final String columnLabel, final Map<String, Class<?>> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Ref getRef(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Blob getBlob(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Clob getClob(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Array getArray(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(final int columnIndex, final Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(final String columnLabel, final Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(final int columnIndex, final Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(final String columnLabel, final Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getURL(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRef(final int columnIndex, final Ref x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRef(final String columnLabel, final Ref x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(final int columnIndex, final Blob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(final String columnLabel, final Blob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(final int columnIndex, final Clob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(final String columnLabel, final Clob x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(final int columnIndex, final Array x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateArray(final String columnLabel, final Array x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RowId getRowId(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(final int columnIndex, final RowId x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRowId(final String columnLabel, final RowId x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHoldability() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNString(final int columnIndex, final String nString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNString(final String columnLabel, final String nString) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(final String columnLabel, final NClob nClob) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NClob getNClob(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SQLXML getSQLXML(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNString(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNString(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getNCharacterStream(final int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getNCharacterStream(final String columnLabel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(
            final String columnLabel, final Reader reader, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(
            final String columnLabel, final InputStream x, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(
            final String columnLabel, final InputStream x, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(
            final String columnLabel, final Reader reader, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(
            final int columnIndex, final InputStream inputStream, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(
            final String columnLabel, final InputStream inputStream, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(final int columnIndex, final Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateClob(final String columnLabel, final Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(final int columnIndex, final Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateNClob(final String columnLabel, final Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) {
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
