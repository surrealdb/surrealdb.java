package com.surrealdb;

import java.util.Objects;

/**
 * Represents a range of record IDs on a table for range queries (e.g.
 * select/update/delete/upsert over IDs between a start and an end bound, either
 * of which may be unbounded).
 * <p>
 * Use {@link Id#from(long)}, {@link Id#from(String)}, or
 * {@link Id#from(java.util.UUID)} for bounds. Pass {@code null} for start or
 * end to make that bound unbounded.
 * <p>
 * {@link #RecordIdRange(String, Id, Id)} includes both bounds
 * ({@code table:start..=end}). Use
 * {@link #RecordIdRange(String, Id, boolean, Id, boolean)} to exclude either
 * bound, for example {@code new RecordIdRange("t", a, true, b, false)} is
 * SurrealQL's {@code t:a..b}.
 */
public final class RecordIdRange {

	/** Bit flag passed to the native layer: the start bound is exclusive. */
	static final int START_EXCLUSIVE = 1;

	/** Bit flag passed to the native layer: the end bound is exclusive. */
	static final int END_EXCLUSIVE = 2;

	private final String table;
	private final Id start;
	private final boolean startInclusive;
	private final Id end;
	private final boolean endInclusive;

	/**
	 * Creates a range over the given table with optional start and end bounds, both
	 * inclusive.
	 *
	 * @param table
	 *            table name
	 * @param start
	 *            lower bound (inclusive), or {@code null} for unbounded
	 * @param end
	 *            upper bound (inclusive), or {@code null} for unbounded
	 */
	public RecordIdRange(String table, Id start, Id end) {
		this(table, start, true, end, true);
	}

	/**
	 * Creates a range over the given table, choosing whether each bound is
	 * inclusive or exclusive.
	 *
	 * @param table
	 *            table name
	 * @param start
	 *            lower bound, or {@code null} for unbounded
	 * @param startInclusive
	 *            whether records whose ID equals {@code start} are included
	 * @param end
	 *            upper bound, or {@code null} for unbounded
	 * @param endInclusive
	 *            whether records whose ID equals {@code end} are included
	 */
	public RecordIdRange(String table, Id start, boolean startInclusive, Id end, boolean endInclusive) {
		this.table = Objects.requireNonNull(table, "table");
		this.start = start;
		this.startInclusive = startInclusive;
		this.end = end;
		this.endInclusive = endInclusive;
	}

	public String getTable() {
		return table;
	}

	/**
	 * Lower bound, or {@code null} if unbounded.
	 */
	public Id getStart() {
		return start;
	}

	/**
	 * Whether the lower bound is inclusive. Meaningless when the range has no lower
	 * bound.
	 */
	public boolean isStartInclusive() {
		return startInclusive;
	}

	/**
	 * Upper bound, or {@code null} if unbounded.
	 */
	public Id getEnd() {
		return end;
	}

	/**
	 * Whether the upper bound is inclusive. Meaningless when the range has no upper
	 * bound.
	 */
	public boolean isEndInclusive() {
		return endInclusive;
	}

	long startPtr() {
		return start != null ? start.getPtr() : 0;
	}

	long endPtr() {
		return end != null ? end.getPtr() : 0;
	}

	int bounds() {
		return (startInclusive ? 0 : START_EXCLUSIVE) | (endInclusive ? 0 : END_EXCLUSIVE);
	}
}
