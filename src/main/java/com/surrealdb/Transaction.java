package com.surrealdb;

import java.util.Map;
import java.util.Objects;

/**
 * Client-side transaction. Use {@link Surreal#beginTransaction()} to start a
 * transaction. Operations (e.g. {@link #query(String)}) run within the
 * transaction until {@link #commit()} or {@link #cancel()} is called.
 */
public class Transaction extends Native {

	Transaction(long ptr) {
		super(ptr);
	}

	private static native void nativeDeleteInstance(long ptr);

	private native boolean commit(long ptr);

	private native boolean cancel(long ptr);

	private native long query(long ptr, String sql);

	private native long queryWithBindings(long ptr, String sql, String[] paramsKey, long[] valuePtrs);

	/**
	 * Commits the transaction. After this call, the transaction is completed and
	 * must not be used. On failure the native layer throws; the return value is
	 * only true on success.
	 */
	public void commit() {
		commit(takePtr());
	}

	/**
	 * Cancels (rolls back) the transaction. After this call, the transaction is
	 * completed and must not be used. On failure the native layer throws; the
	 * return value is only true on success.
	 */
	public void cancel() {
		cancel(takePtr());
	}

	/**
	 * Runs a SurrealQL query within this transaction.
	 *
	 * @param sql
	 *            the SurrealQL query
	 * @return the query response
	 */
	public Response query(String sql) {
		return new Response(query(getPtr(), sql));
	}

	/**
	 * Runs a parameterized SurrealQL query within this transaction.
	 *
	 * @param sql
	 *            the SurrealQL query
	 * @param params
	 *            a map containing parameter values to be bound to the SQL query
	 * @return the query response
	 */
	public Response query(String sql, Map<String, ?> params) {
		final Map<String, ValueMut> valueMuts = ValueBuilder.convertParams(params);
		try {
			final String[] keys = valueMuts.keySet().toArray(new String[0]);
			final long[] values = new long[keys.length];
			for (int i = 0; i < keys.length; i++) {
				values[i] = valueMuts.get(keys[i]).getPtr();
			}
			return new Response(queryWithBindings(getPtr(), sql, keys, values));
		} finally {
			ValueBuilder.releaseParams(valueMuts, params);
		}
	}

	@Override
	final NativeCleaner.Deleter deleter() {
		return Transaction::nativeDeleteInstance;
	}

	@Override
	final String toString(long ptr) {
		return getClass().getName() + "[ptr=" + ptr + "]";
	}

	@Override
	final int hashCode(long ptr) {
		return Objects.hashCode(ptr);
	}

	@Override
	final boolean equals(long ptr1, long ptr2) {
		return ptr1 == ptr2;
	}
}
