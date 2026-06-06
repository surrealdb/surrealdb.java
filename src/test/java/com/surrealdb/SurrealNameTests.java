package com.surrealdb;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class SurrealNameTests {

	private static final ZonedDateTime CREATED_AT = ZonedDateTime.of(2026, 6, 6, 12, 30, 0, 0, ZoneId.of("UTC"));
	private static final ZonedDateTime RAW_CREATED_AT = ZonedDateTime.of(1999, 1, 2, 3, 4, 5, 0, ZoneId.of("UTC"));

	public static final class AnnotatedProfile {

		@SurrealName("first_name")
		private String firstName;

		@SurrealName("created_at")
		private ZonedDateTime createdAt;

		@SurrealName("login_count")
		private int loginCount;

		@SurrealName("active_flag")
		private boolean activeFlag;

		@SurrealName("credit_limit")
		private BigDecimal creditLimit;

		@SurrealName("tag_names")
		private List<String> tagNames;

		@SurrealName("optional_note")
		private Optional<String> optionalNote;

		private String nickname;

		public AnnotatedProfile() {
		}

		AnnotatedProfile(String firstName, ZonedDateTime createdAt, int loginCount, boolean activeFlag,
				BigDecimal creditLimit, List<String> tagNames, Optional<String> optionalNote, String nickname) {
			this.firstName = firstName;
			this.createdAt = createdAt;
			this.loginCount = loginCount;
			this.activeFlag = activeFlag;
			this.creditLimit = creditLimit;
			this.tagNames = tagNames;
			this.optionalNote = optionalNote;
			this.nickname = nickname;
		}
	}

	public static final class RawNameProfile {
		private String firstName;
		private ZonedDateTime createdAt;
		private int loginCount;
		private boolean activeFlag;
		private BigDecimal creditLimit;
		private List<String> tagNames;
		private Optional<String> optionalNote;

		public RawNameProfile() {
		}
	}

	public static final class IgnoredMembersProfile {
		@SurrealName("instance_name")
		private String instanceName;

		@SurrealName("cached_name")
		private static String cachedName = "must not serialize";

		@SurrealName("temporary_name")
		private transient String temporaryName;

		IgnoredMembersProfile(String instanceName, String temporaryName) {
			this.instanceName = instanceName;
			this.temporaryName = temporaryName;
		}
	}

	public static final class DuplicateSurrealNameProfile {
		@SurrealName("duplicated_name")
		private String firstName = "first";

		@SurrealName("duplicated_name")
		private String secondName = "second";

		public DuplicateSurrealNameProfile() {
		}
	}

	public static final class BlankSurrealNameProfile {
		@SurrealName(" ")
		private String firstName = "Ada";

		public BlankSurrealNameProfile() {
		}
	}

	@Test
	void serializesAnnotatedFieldsUsingSurrealNames() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal.queryBind("RETURN $profile", Collections.singletonMap("profile", profile()))
					.take(0);
			final List<String> keys = keys(value.getObject());

			assertEquals(Arrays.asList("active_flag", "created_at", "credit_limit", "first_name", "login_count",
					"nickname", "optional_note", "tag_names"), keys);
			assertFalse(keys.contains("activeFlag"));
			assertFalse(keys.contains("createdAt"));
			assertFalse(keys.contains("creditLimit"));
			assertFalse(keys.contains("firstName"));
			assertFalse(keys.contains("loginCount"));
			assertFalse(keys.contains("optionalNote"));
			assertFalse(keys.contains("tagNames"));
		}
	}

	@Test
	void deserializesAnnotatedFieldsFromSurrealNames() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal
					.queryBind("RETURN $profile", Collections.singletonMap("profile", annotatedMap())).take(0);

			assertAnnotatedProfile(value.get(AnnotatedProfile.class), "Grace", CREATED_AT, 11, true,
					new BigDecimal("999.50"), Arrays.asList("snake", "case"), Optional.of("read from explicit name"),
					"g");
		}
	}

	@Test
	void explicitSurrealNameWinsWhenRawAndExplicitKeysAreBothPresent() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Map<String, java.lang.Object> values = annotatedMap();
			values.put("firstName", "Raw");
			values.put("createdAt", RAW_CREATED_AT);
			values.put("loginCount", 1);
			values.put("activeFlag", false);
			values.put("creditLimit", new BigDecimal("1.00"));
			values.put("tagNames", Collections.singletonList("raw"));
			values.put("optionalNote", "raw note");
			final Value value = surreal.queryBind("RETURN $profile", Collections.singletonMap("profile", values))
					.take(0);

			assertAnnotatedProfile(value.get(AnnotatedProfile.class), "Grace", CREATED_AT, 11, true,
					new BigDecimal("999.50"), Arrays.asList("snake", "case"), Optional.of("read from explicit name"),
					"g");
		}
	}

	@Test
	void roundTripsAnnotatedObjectThroughCreateAndSelectUsingSurrealNames() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final AnnotatedProfile created = surreal.create(AnnotatedProfile.class, "annotated_profile", profile())
					.get(0);
			assertAnnotatedProfile(created, "Ada", CREATED_AT, 7, true, new BigDecimal("123.45"),
					Arrays.asList("admin", "beta"), Optional.of("explicitly named"), "ace");

			final Value selected = surreal.query("SELECT * FROM annotated_profile").take(0).getArray().get(0);
			final List<String> keys = keys(selected.getObject());
			assertTrue(keys.contains("first_name"));
			assertTrue(keys.contains("created_at"));
			assertTrue(keys.contains("login_count"));
			assertTrue(keys.contains("active_flag"));
			assertTrue(keys.contains("credit_limit"));
			assertTrue(keys.contains("tag_names"));
			assertTrue(keys.contains("optional_note"));
			assertTrue(keys.contains("nickname"));
			assertFalse(keys.contains("firstName"));
			assertFalse(keys.contains("createdAt"));
			assertFalse(keys.contains("loginCount"));
			assertFalse(keys.contains("activeFlag"));
			assertFalse(keys.contains("creditLimit"));
			assertFalse(keys.contains("tagNames"));
			assertFalse(keys.contains("optionalNote"));
		}
	}

	@Test
	void keepsRawFieldNameBehaviorForUnannotatedFields() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal.queryBind("RETURN $profile", Collections.singletonMap("profile", rawNameMap()))
					.take(0);
			final RawNameProfile profile = value.get(RawNameProfile.class);

			assertEquals("Ada", profile.firstName);
			assertEquals(CREATED_AT, profile.createdAt);
			assertEquals(7, profile.loginCount);
			assertTrue(profile.activeFlag);
			assertEquals(0, new BigDecimal("123.45").compareTo(profile.creditLimit));
			assertEquals(Arrays.asList("admin", "beta"), profile.tagNames);
			assertEquals(Optional.of("raw names remain supported"), profile.optionalNote);
		}
	}

	@Test
	void keepsIgnoringUnknownDatabaseKeysForAnnotatedTypes() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Map<String, java.lang.Object> values = annotatedMap();
			values.put("unknown_name", "ignored");
			final Value value = surreal.queryBind("RETURN $profile", Collections.singletonMap("profile", values))
					.take(0);

			assertAnnotatedProfile(value.get(AnnotatedProfile.class), "Grace", CREATED_AT, 11, true,
					new BigDecimal("999.50"), Arrays.asList("snake", "case"), Optional.of("read from explicit name"),
					"g");
		}
	}

	@Test
	void keepsIgnoringStaticAndTransientFieldsEvenWhenAnnotated() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final IgnoredMembersProfile profile = new IgnoredMembersProfile("persisted", "temporary");
			final Value value = surreal.queryBind("RETURN $profile", Collections.singletonMap("profile", profile))
					.take(0);
			final List<String> keys = keys(value.getObject());

			assertEquals(Collections.singletonList("instance_name"), keys);
		}
	}

	@Test
	void rejectsDuplicateSurrealNamesDuringSerialization() {
		try (final Surreal surreal = openMemoryDatabase()) {
			assertThrows(SurrealException.class, () -> surreal.queryBind("RETURN $profile",
					Collections.singletonMap("profile", new DuplicateSurrealNameProfile())));
		}
	}

	@Test
	void rejectsDuplicateSurrealNamesDuringDeserialization() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal
					.queryBind("RETURN $profile",
							Collections.singletonMap("profile", Collections.singletonMap("duplicated_name", "value")))
					.take(0);

			assertThrows(SurrealException.class, () -> value.get(DuplicateSurrealNameProfile.class));
		}
	}

	@Test
	void rejectsBlankSurrealNameDuringSerialization() {
		try (final Surreal surreal = openMemoryDatabase()) {
			assertThrows(SurrealException.class, () -> surreal.queryBind("RETURN $profile",
					Collections.singletonMap("profile", new BlankSurrealNameProfile())));
		}
	}

	@Test
	void rejectsBlankSurrealNameDuringDeserialization() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal.queryBind("RETURN $profile",
					Collections.singletonMap("profile", Collections.singletonMap("firstName", "Ada"))).take(0);

			assertThrows(SurrealException.class, () -> value.get(BlankSurrealNameProfile.class));
		}
	}

	private static Surreal openMemoryDatabase() {
		final Surreal surreal = new Surreal();
		surreal.connect("memory").useNs("surreal_name_tests").useDb("surreal_name_tests");
		return surreal;
	}

	private static AnnotatedProfile profile() {
		return new AnnotatedProfile("Ada", CREATED_AT, 7, true, new BigDecimal("123.45"),
				Arrays.asList("admin", "beta"), Optional.of("explicitly named"), "ace");
	}

	private static Map<String, java.lang.Object> annotatedMap() {
		final Map<String, java.lang.Object> values = new LinkedHashMap<>();
		values.put("first_name", "Grace");
		values.put("created_at", CREATED_AT);
		values.put("login_count", 11);
		values.put("active_flag", true);
		values.put("credit_limit", new BigDecimal("999.50"));
		values.put("tag_names", Arrays.asList("snake", "case"));
		values.put("optional_note", "read from explicit name");
		values.put("nickname", "g");
		return values;
	}

	private static Map<String, java.lang.Object> rawNameMap() {
		final Map<String, java.lang.Object> values = new LinkedHashMap<>();
		values.put("firstName", "Ada");
		values.put("createdAt", CREATED_AT);
		values.put("loginCount", 7);
		values.put("activeFlag", true);
		values.put("creditLimit", new BigDecimal("123.45"));
		values.put("tagNames", Arrays.asList("admin", "beta"));
		values.put("optionalNote", "raw names remain supported");
		return values;
	}

	private static List<String> keys(final Object object) {
		final List<String> keys = new ArrayList<>();
		for (final Entry entry : object) {
			keys.add(entry.getKey());
		}
		Collections.sort(keys);
		return keys;
	}

	private static void assertAnnotatedProfile(final AnnotatedProfile actual, final String firstName,
			final ZonedDateTime createdAt, final int loginCount, final boolean activeFlag, final BigDecimal creditLimit,
			final List<String> tagNames, final Optional<String> optionalNote, final String nickname) {
		assertNotNull(actual);
		assertEquals(firstName, actual.firstName);
		assertEquals(createdAt, actual.createdAt);
		assertEquals(loginCount, actual.loginCount);
		assertEquals(activeFlag, actual.activeFlag);
		assertNotNull(actual.creditLimit);
		assertEquals(0, creditLimit.compareTo(actual.creditLimit));
		assertEquals(tagNames, actual.tagNames);
		assertEquals(optionalNote, actual.optionalNote);
		assertEquals(nickname, actual.nickname);
	}
}
