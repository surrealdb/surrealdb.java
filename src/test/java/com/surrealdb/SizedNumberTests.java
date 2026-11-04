package com.surrealdb;

import java.math.BigInteger;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.surrealdb.pojos.SizedNumbers;

/**
 * Sized number kinds (SurrealDB 4.0): i8, u8, i16, u16, i32, u32, u64, f16 and
 * f32, next to the historical int, float and decimal.
 */
public class SizedNumberTests {

	private static final BigInteger U64_MAX = new BigInteger("18446744073709551615");

	private static Surreal connect() {
		final Surreal surreal = new Surreal();
		surreal.connect("memory").useNs("test_ns").useDb("test_db");
		return surreal;
	}

	private static Value eval(final Surreal surreal, final String expression) {
		return surreal.query("RETURN " + expression).take(0);
	}

	private static String typeOf(final Surreal surreal, final ValueMut value) {
		return surreal.query("RETURN type::of($v)", Collections.singletonMap("v", value)).take(0).getString();
	}

	// ------------------------------------------------------------------
	// Read side
	// ------------------------------------------------------------------

	@Test
	void readIntegerKinds() {
		try (final Surreal surreal = connect()) {
			final java.lang.Object[][] cases = {{"-128i8", NumberType.I8, -128L}, {"255u8", NumberType.U8, 255L},
					{"-32768i16", NumberType.I16, -32768L}, {"65535u16", NumberType.U16, 65535L},
					{"-2147483648i32", NumberType.I32, -2147483648L}, {"4294967295u32", NumberType.U32, 4294967295L},
					{"-9223372036854775808i64", NumberType.INT, Long.MIN_VALUE},
					{"9223372036854775807u64", NumberType.U64, Long.MAX_VALUE}};
			for (final java.lang.Object[] c : cases) {
				final Value v = eval(surreal, (String) c[0]);
				final String label = (String) c[0];
				assertTrue(v.isNumber(), label);
				assertEquals(c[1], v.getNumberType(), label);
				assertTrue(v.isLong(), label);
				assertFalse(v.isBigInteger(), label);
				assertFalse(v.isDouble(), label);
				assertFalse(v.isBigDecimal(), label);
				assertEquals(((Long) c[2]).longValue(), v.getLong(), label);
				assertEquals(BigInteger.valueOf((Long) c[2]), v.getBigInteger(), label);
				assertEquals(((NumberType) c[1]).getSurrealName(), eval(surreal, "type::of(" + c[0] + ")").getString());
			}
		}
	}

	@Test
	void readFloatKinds() {
		try (final Surreal surreal = connect()) {
			final java.lang.Object[][] cases = {{"1.5f16", NumberType.F16, 1.5d}, {"-0.25f32", NumberType.F32, -0.25d},
					{"1.5f64", NumberType.FLOAT, 1.5d}, {"1.5f", NumberType.FLOAT, 1.5d},
					// 65504 is the largest finite f16
					{"65504f16", NumberType.F16, 65504d},};
			for (final java.lang.Object[] c : cases) {
				final Value v = eval(surreal, (String) c[0]);
				final String label = (String) c[0];
				assertTrue(v.isNumber(), label);
				assertEquals(c[1], v.getNumberType(), label);
				assertTrue(v.isDouble(), label);
				assertFalse(v.isLong(), label);
				assertFalse(v.isBigInteger(), label);
				assertEquals((Double) c[2], v.getDouble(), 0d, label);
				assertTrue(v.getNumberType().isFloatingPoint(), label);
			}
		}
	}

	@Test
	void readBigDecimal() {
		try (final Surreal surreal = connect()) {
			final String text = "123456789012345678901234567890123456789.5";
			final Value v = eval(surreal, "<bigdecimal> \"" + text + "\"");
			assertTrue(v.isNumber());
			assertEquals(NumberType.BIGDECIMAL, v.getNumberType());
			assertTrue(v.isBigDecimal());
			assertFalse(v.isLong());
			assertFalse(v.isDouble());
			assertFalse(v.getNumberType().isInteger());
			assertEquals(new java.math.BigDecimal(text), v.getBigDecimal());
		}
	}

	@Test
	void historicalKindsAreUnchanged() {
		try (final Surreal surreal = connect()) {
			final Value i = eval(surreal, "5");
			assertEquals(NumberType.INT, i.getNumberType());
			assertTrue(i.isLong());
			assertFalse(i.isDouble());
			assertEquals(5L, i.getLong());

			final Value f = eval(surreal, "5.5f");
			assertEquals(NumberType.FLOAT, f.getNumberType());
			assertTrue(f.isDouble());
			assertFalse(f.isLong());
			assertEquals(5.5d, f.getDouble(), 0d);

			final Value d = eval(surreal, "5.5dec");
			assertEquals(NumberType.DECIMAL, d.getNumberType());
			assertTrue(d.isBigDecimal());
			assertFalse(d.isLong());
			assertFalse(d.isDouble());
			assertFalse(d.isBigInteger());
		}
	}

	@Test
	void numberTypeOnNonNumberThrows() {
		try (final Surreal surreal = connect()) {
			final Value s = eval(surreal, "'text'");
			assertFalse(s.isNumber());
			assertThrows(NullPointerException.class, s::getNumberType);
			assertThrows(NullPointerException.class, s::getBigInteger);
		}
	}

	@Test
	void u64AboveLongMaxIsNotALong() {
		try (final Surreal surreal = connect()) {
			// Long.MAX_VALUE + 1 and u64::MAX
			for (final String literal : new String[]{"9223372036854775808u64", "18446744073709551615u64"}) {
				final Value v = eval(surreal, literal);
				assertEquals(NumberType.U64, v.getNumberType(), literal);
				assertFalse(v.isLong(), literal);
				assertFalse(v.isDouble(), literal);
				assertTrue(v.isBigInteger(), literal);
				assertEquals(new BigInteger(literal.replace("u64", "")), v.getBigInteger(), literal);
				// Must fail loudly instead of wrapping into a negative long
				final SurrealException e = assertThrows(SurrealException.class, v::getLong, literal);
				assertTrue(e.getMessage().contains("getBigInteger"), e.getMessage());
			}
			assertEquals(U64_MAX, eval(surreal, "18446744073709551615u64").getBigInteger());
		}
	}

	@Test
	void sizedKindsKeepTheirWidthWhenStored() {
		try (final Surreal surreal = connect()) {
			final Response r = surreal.query("CREATE sized:1 SET a = 5i8, b = 200u8, c = 1.5f32, d = 2.5f16, e = 7u64;"
					+ "SELECT type::of(a) AS a, type::of(b) AS b, type::of(c) AS c, type::of(d) AS d, type::of(e) AS e FROM sized;"
					+ "SELECT * FROM sized;");
			final Object row = r.take(1).getArray().get(0).getObject();
			assertEquals("i8", row.get("a").getString());
			assertEquals("u8", row.get("b").getString());
			assertEquals("f32", row.get("c").getString());
			assertEquals("f16", row.get("d").getString());
			assertEquals("u64", row.get("e").getString());
			final Object stored = r.take(2).getArray().get(0).getObject();
			assertEquals(NumberType.I8, stored.get("a").getNumberType());
			assertEquals(NumberType.U8, stored.get("b").getNumberType());
			assertEquals(NumberType.F32, stored.get("c").getNumberType());
			assertEquals(NumberType.F16, stored.get("d").getNumberType());
			assertEquals(NumberType.U64, stored.get("e").getNumberType());
		}
	}

	@Test
	void typedFieldProducesSizedKind() {
		try (final Surreal surreal = connect()) {
			surreal.query("DEFINE TABLE typed SCHEMAFULL;" + "DEFINE FIELD small ON typed TYPE i8;"
					+ "DEFINE FIELD half ON typed TYPE f16;" + "DEFINE FIELD big ON typed TYPE u64;");
			final Response r = surreal.query("CREATE typed:1 SET small = 7, half = 0.5, big = 18446744073709551615u64;"
					+ "SELECT * FROM typed;");
			final Object stored = r.take(1).getArray().get(0).getObject();
			assertEquals(NumberType.I8, stored.get("small").getNumberType());
			assertEquals(7L, stored.get("small").getLong());
			assertEquals(NumberType.F16, stored.get("half").getNumberType());
			assertEquals(0.5d, stored.get("half").getDouble(), 0d);
			assertEquals(NumberType.U64, stored.get("big").getNumberType());
			assertEquals(U64_MAX, stored.get("big").getBigInteger());
		}
	}

	// ------------------------------------------------------------------
	// POJO conversion
	// ------------------------------------------------------------------

	@Test
	void pojoIsFilledFromSizedNumbers() {
		try (final Surreal surreal = connect()) {
			final Value v = eval(surreal,
					"{ bytePrimitive: -5i8, byteObject: 100u8, shortPrimitive: -300i16, shortObject: 30000u16,"
							+ " intPrimitive: -70000i32, intObject: 2000000000u32,"
							+ " longPrimitive: 9223372036854775807u64, longObject: 4294967295u32,"
							+ " floatPrimitive: 1.5f32, floatObject: 2.5f16,"
							+ " doublePrimitive: 0.25f32, doubleObject: 1.5f16,"
							+ " bigInteger: 18446744073709551615u64 }");
			final SizedNumbers n = v.get(SizedNumbers.class);
			assertEquals((byte) -5, n.bytePrimitive);
			assertEquals(Byte.valueOf((byte) 100), n.byteObject);
			assertEquals((short) -300, n.shortPrimitive);
			assertEquals(Short.valueOf((short) 30000), n.shortObject);
			assertEquals(-70000, n.intPrimitive);
			assertEquals(Integer.valueOf(2000000000), n.intObject);
			assertEquals(Long.MAX_VALUE, n.longPrimitive);
			assertEquals(Long.valueOf(4294967295L), n.longObject);
			assertEquals(1.5f, n.floatPrimitive, 0f);
			assertEquals(Float.valueOf(2.5f), n.floatObject);
			assertEquals(0.25d, n.doublePrimitive, 0d);
			assertEquals(Double.valueOf(1.5d), n.doubleObject);
			assertEquals(U64_MAX, n.bigInteger);
		}
	}

	@Test
	void pojoNumericFieldsAcceptAnyNumericKind() {
		try (final Surreal surreal = connect()) {
			// Integer kinds into float and double fields, float kinds into float and double
			final Value v = eval(surreal,
					"{ floatPrimitive: 7u8, floatObject: 9i16, doublePrimitive: 3i32, doubleObject: 4u32,"
							+ " longPrimitive: 12i8, intPrimitive: 13u16, shortPrimitive: 14u8, bytePrimitive: 15u8 }");
			final SizedNumbers n = v.get(SizedNumbers.class);
			assertEquals(7f, n.floatPrimitive, 0f);
			assertEquals(Float.valueOf(9f), n.floatObject);
			assertEquals(3d, n.doublePrimitive, 0d);
			assertEquals(Double.valueOf(4d), n.doubleObject);
			assertEquals(12L, n.longPrimitive);
			assertEquals(13, n.intPrimitive);
			assertEquals((short) 14, n.shortPrimitive);
			assertEquals((byte) 15, n.bytePrimitive);
		}
	}

	@Test
	void pojoLongFieldRejectsU64AboveLongMax() {
		try (final Surreal surreal = connect()) {
			final Value v = eval(surreal, "{ longPrimitive: 18446744073709551615u64 }");
			final SurrealException e = assertThrows(SurrealException.class, () -> v.get(SizedNumbers.class));
			assertTrue(e.getMessage().contains("18446744073709551615"), e.getMessage());
		}
	}

	// ------------------------------------------------------------------
	// Write side
	// ------------------------------------------------------------------

	@Test
	void factoriesProduceTheMatchingKind() {
		try (final Surreal surreal = connect()) {
			assertEquals("i8", typeOf(surreal, ValueMut.createI8((byte) -128)));
			assertEquals("u8", typeOf(surreal, ValueMut.createU8(255)));
			assertEquals("i16", typeOf(surreal, ValueMut.createI16((short) -32768)));
			assertEquals("u16", typeOf(surreal, ValueMut.createU16(65535)));
			assertEquals("i32", typeOf(surreal, ValueMut.createI32(Integer.MIN_VALUE)));
			assertEquals("u32", typeOf(surreal, ValueMut.createU32(4294967295L)));
			assertEquals("u64", typeOf(surreal, ValueMut.createU64(Long.MAX_VALUE)));
			assertEquals("u64", typeOf(surreal, ValueMut.createU64(U64_MAX)));
			assertEquals("f32", typeOf(surreal, ValueMut.createF32(1.5f)));
			assertEquals("f16", typeOf(surreal, ValueMut.createF16(1.5f)));
		}
	}

	@Test
	void factoriesRoundTripTheValue() {
		try (final Surreal surreal = connect()) {
			final java.lang.Object[][] cases = {{ValueMut.createI8((byte) -128), NumberType.I8, "-128"},
					{ValueMut.createU8(255), NumberType.U8, "255"}, {ValueMut.createU8(0), NumberType.U8, "0"},
					{ValueMut.createI16((short) -32768), NumberType.I16, "-32768"},
					{ValueMut.createU16(65535), NumberType.U16, "65535"},
					{ValueMut.createI32(Integer.MIN_VALUE), NumberType.I32, "-2147483648"},
					{ValueMut.createU32(4294967295L), NumberType.U32, "4294967295"},
					{ValueMut.createU64(0L), NumberType.U64, "0"},
					{ValueMut.createU64(Long.MAX_VALUE), NumberType.U64, "9223372036854775807"},
					{ValueMut.createU64(new BigInteger("9223372036854775808")), NumberType.U64, "9223372036854775808"},
					{ValueMut.createU64(U64_MAX), NumberType.U64, "18446744073709551615"}};
			for (final java.lang.Object[] c : cases) {
				final Value v = surreal.query("RETURN $v", Collections.singletonMap("v", c[0])).take(0);
				assertEquals(c[1], v.getNumberType(), (String) c[2]);
				assertEquals(new BigInteger((String) c[2]), v.getBigInteger(), (String) c[2]);
			}
			final Value f32 = surreal.query("RETURN $v", Collections.singletonMap("v", ValueMut.createF32(0.1f)))
					.take(0);
			assertEquals(NumberType.F32, f32.getNumberType());
			// f32 0.1 widens exactly, it is not the f64 0.1
			assertEquals((double) 0.1f, f32.getDouble(), 0d);
			final Value f16 = surreal.query("RETURN $v", Collections.singletonMap("v", ValueMut.createF16(65504f)))
					.take(0);
			assertEquals(NumberType.F16, f16.getNumberType());
			assertEquals(65504d, f16.getDouble(), 0d);
			// f16 rounds to the nearest representable value
			final Value rounded = surreal.query("RETURN $v", Collections.singletonMap("v", ValueMut.createF16(1.0001f)))
					.take(0);
			assertEquals(1d, rounded.getDouble(), 0d);
		}
	}

	@Test
	void factoriesPersistTheKind() {
		try (final Surreal surreal = connect()) {
			final java.util.Map<String, java.lang.Object> vars = new java.util.HashMap<>();
			vars.put("a", ValueMut.createI8((byte) 5));
			vars.put("b", ValueMut.createU32(3000000000L));
			vars.put("c", ValueMut.createF32(2.5f));
			surreal.query("CREATE persisted:1 SET a = $a, b = $b, c = $c", vars);
			final Object row = surreal.query("SELECT * FROM persisted").take(0).getArray().get(0).getObject();
			assertEquals(NumberType.I8, row.get("a").getNumberType());
			assertEquals(NumberType.U32, row.get("b").getNumberType());
			assertEquals(3000000000L, row.get("b").getLong());
			assertEquals(NumberType.F32, row.get("c").getNumberType());
			assertEquals(2.5d, row.get("c").getDouble(), 0d);
		}
	}

	@Test
	void factoriesFillTypedSchemaFields() {
		try (final Surreal surreal = connect()) {
			surreal.query("DEFINE TABLE strict SCHEMAFULL;" + "DEFINE FIELD small ON strict TYPE u8;"
					+ "DEFINE FIELD wide ON strict TYPE u64;");
			final java.util.Map<String, java.lang.Object> vars = new java.util.HashMap<>();
			vars.put("small", ValueMut.createU8(200));
			vars.put("wide", ValueMut.createU64(U64_MAX));
			surreal.query("CREATE strict:1 SET small = $small, wide = $wide", vars);
			final Object row = surreal.query("SELECT * FROM strict").take(0).getArray().get(0).getObject();
			assertEquals(NumberType.U8, row.get("small").getNumberType());
			assertEquals(U64_MAX, row.get("wide").getBigInteger());
		}
	}

	@Test
	void unsignedFactoriesValidateTheirRange() {
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU8(-1));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU8(256));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU16(-1));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU16(65536));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU32(-1L));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU32(4294967296L));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU64(-1L));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU64(BigInteger.valueOf(-1)));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createU64(U64_MAX.add(BigInteger.ONE)));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createF16(65520f));
		assertThrows(IllegalArgumentException.class, () -> ValueMut.createF16(-1e10f));
	}

	@Test
	void f16KeepsNonFiniteValues() {
		try (final Surreal surreal = connect()) {
			for (final float f : new float[]{Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN}) {
				final Value v = surreal.query("RETURN $v", Collections.singletonMap("v", ValueMut.createF16(f)))
						.take(0);
				assertEquals(NumberType.F16, v.getNumberType());
				assertEquals((double) f, v.getDouble(), 0d);
			}
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"ws", "http"})
	void remoteResultsKeepTheirKindAndBoundValuesAreWidened(final String protocol) {
		final String url = protocol.equals("ws") ? TestServer.WS_URL : TestServer.HTTP_URL;
		try (final Surreal surreal = TestServer.connect(url)) {
			Assumptions.assumeTrue(Surreal.atLeast(surreal.version(), 4, 0), "sized numbers need server 4.0+");
			assertEquals(NumberType.U8, eval(surreal, "200u8").getNumberType());
			final Value u64 = eval(surreal, "18446744073709551615u64");
			assertEquals(NumberType.U64, u64.getNumberType());
			assertEquals(U64_MAX, u64.getBigInteger());
			assertEquals(NumberType.F16, eval(surreal, "1.5f16").getNumberType());
			assertEquals(NumberType.F32, eval(surreal, "1.5f32").getNumberType());
			// Bound sized values are widened on the wire.
			assertEquals("int", typeOf(surreal, ValueMut.createU8(200)));
			assertEquals("decimal", typeOf(surreal, ValueMut.createU64(U64_MAX)));
			assertEquals("float", typeOf(surreal, ValueMut.createF32(1.5f)));
			// A typed schema field makes the stored kind sized again.
			surreal.query("DEFINE TABLE sized SCHEMAFULL; DEFINE FIELD a ON sized TYPE u8");
			surreal.query("CREATE sized:1 SET a = $a", Collections.singletonMap("a", ValueMut.createU8(200)));
			assertEquals(NumberType.U8, surreal.query("SELECT VALUE a FROM ONLY sized:1").take(0).getNumberType());
		}
	}

	@Test
	void existingJavaTypesStillMapToIntAndFloat() {
		try (final Surreal surreal = connect()) {
			assertEquals("int", typeOf(surreal, ValueMut.createLong(5)));
			assertEquals("float", typeOf(surreal, ValueMut.createDouble(5.5)));
			final java.util.Map<String, java.lang.Object> vars = new java.util.HashMap<>();
			vars.put("short", (short) 2);
			vars.put("int", 3);
			vars.put("long", 4L);
			vars.put("float", 5.5f);
			vars.put("double", 6.5d);
			vars.put("decimal", new java.math.BigDecimal("7.5"));
			final Value types = surreal
					.query("RETURN [type::of($short), type::of($int),"
							+ " type::of($long), type::of($float), type::of($double), type::of($decimal)]", vars)
					.take(0);
			final String[] expected = {"int", "int", "int", "float", "float", "decimal"};
			for (int i = 0; i < expected.length; i++) {
				assertEquals(expected[i], types.getArray().get(i).getString(), "index " + i);
			}
		}
	}
}
