package com.surrealdb;

/**
 * The exact numeric kind of a SurrealDB number, as returned by
 * {@link Value#getNumberType()}.
 * <p>
 * SurrealDB 4.0 adds sized integer and floating-point kinds next to the
 * historical {@link #INT}, {@link #FLOAT} and {@link #DECIMAL} kinds. A 3.x
 * server only ever produces those three.
 * <p>
 * {@link Value#isLong()} and {@link Value#isDouble()} stay true for every
 * integer, respectively floating-point, kind, so code that does not care about
 * the width does not need this enum.
 */
public enum NumberType {
	/** 8-bit signed integer (<code>i8</code>). */
	I8("i8"),
	/** 8-bit unsigned integer (<code>u8</code>). */
	U8("u8"),
	/** 16-bit signed integer (<code>i16</code>). */
	I16("i16"),
	/** 16-bit unsigned integer (<code>u16</code>). */
	U16("u16"),
	/** 32-bit signed integer (<code>i32</code>). */
	I32("i32"),
	/** 32-bit unsigned integer (<code>u32</code>). */
	U32("u32"),
	/** 64-bit signed integer (<code>int</code>, alias <code>i64</code>). */
	INT("int"),
	/**
	 * 64-bit unsigned integer (<code>u64</code>). Values above
	 * {@link Long#MAX_VALUE} do not fit a Java <code>long</code>, see
	 * {@link Value#isBigInteger()}.
	 */
	U64("u64"),
	/** 16-bit IEEE 754 floating point (<code>f16</code>). */
	F16("f16"),
	/** 32-bit IEEE 754 floating point (<code>f32</code>). */
	F32("f32"),
	/**
	 * 64-bit IEEE 754 floating point (<code>float</code>, alias <code>f64</code>).
	 */
	FLOAT("float"),
	/** Arbitrary-precision decimal (<code>decimal</code>). */
	DECIMAL("decimal");

	private final String surrealName;

	NumberType(String surrealName) {
		this.surrealName = surrealName;
	}

	/**
	 * The SurrealQL name of this kind, as returned by <code>type::of</code>.
	 */
	public String getSurrealName() {
		return surrealName;
	}

	/**
	 * True for the integer kinds: {@link #I8}, {@link #U8}, {@link #I16},
	 * {@link #U16}, {@link #I32}, {@link #U32}, {@link #INT} and {@link #U64}.
	 */
	public boolean isInteger() {
		switch (this) {
			case F16 :
			case F32 :
			case FLOAT :
			case DECIMAL :
				return false;
			default :
				return true;
		}
	}

	/**
	 * True for the floating-point kinds: {@link #F16}, {@link #F32} and
	 * {@link #FLOAT}.
	 */
	public boolean isFloatingPoint() {
		return this == F16 || this == F32 || this == FLOAT;
	}

	static NumberType fromSurrealName(String name) {
		for (final NumberType type : values()) {
			if (type.surrealName.equals(name)) {
				return type;
			}
		}
		throw new SurrealException("Unknown number kind: " + name);
	}
}
