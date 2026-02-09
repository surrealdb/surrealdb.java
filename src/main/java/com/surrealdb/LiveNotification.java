package com.surrealdb;

/**
 * A single notification from a live query (CREATE, UPDATE, or DELETE).
 */
public class LiveNotification {

	private final String action;
	private final Value value;
	private final String queryId;

	/**
	 * Called from native code only.
	 */
	public LiveNotification(String action, long valuePtr, String queryId) {
		this.action = action;
		this.value = valuePtr != 0 ? new Value(valuePtr) : null;
		this.queryId = queryId;
	}

	/**
	 * The action that caused this notification: "CREATE", "UPDATE", or "DELETE".
	 */
	public String getAction() {
		return action;
	}

	/** The record value (content) for this notification. */
	public Value getValue() {
		return value;
	}

	/** The live query UUID this notification belongs to. */
	public String getQueryId() {
		return queryId;
	}
}
