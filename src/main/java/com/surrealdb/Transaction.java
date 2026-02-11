package com.surrealdb;

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

	private static native boolean commit(long ptr);

	private static native boolean cancel(long ptr);

	private static native long query(long ptr, String sql);

	/**
	 * Commits the transaction. After this call, the transaction is completed and
	 * must not be used. On failure the native layer throws; the return value is
	 * only true on success.
	 */
	public void commit() {
		try {
			commit(getPtr());
		} finally {
			moved();
		}
	}

	/**
	 * Cancels (rolls back) the transaction. After this call, the transaction is
	 * completed and must not be used. On failure the native layer throws; the
	 * return value is only true on success.
	 */
	public void cancel() {
		try {
			cancel(getPtr());
		} finally {
			moved();
		}
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

	@Override
	final void deleteInstance(long ptr) {
		nativeDeleteInstance(ptr);
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
