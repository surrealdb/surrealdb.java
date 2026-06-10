package com.surrealdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 * Serialization of inherited POJO fields (issue #174): the write path walks the
 * user-defined class hierarchy with the same semantics as the read path.
 */
public class InheritedFieldsTests {

	public static class BaseProfile {
		public String slug;
		public static String cached = "must not serialize";
		public transient String temporary = "must not serialize";

		public BaseProfile() {
		}
	}

	public static final class ChildProfile extends BaseProfile {
		public String name;

		public ChildProfile() {
		}
	}

	public static class AnnotatedBaseProfile {
		@SurrealName("created_at")
		public String createdAt;

		public AnnotatedBaseProfile() {
		}
	}

	public static final class AnnotatedChildProfile extends AnnotatedBaseProfile {
		public String name;

		public AnnotatedChildProfile() {
		}
	}

	public static class HiddenBaseProfile {
		public String id;

		public HiddenBaseProfile() {
		}
	}

	public static final class HiddenChildProfile extends HiddenBaseProfile {
		@SurrealName("child_id")
		public String id;

		public HiddenChildProfile() {
		}
	}

	public static class DuplicateBaseProfile {
		public String code;

		public DuplicateBaseProfile() {
		}
	}

	public static final class DuplicateChildProfile extends DuplicateBaseProfile {
		@SurrealName("code")
		public String childCode;

		public DuplicateChildProfile() {
		}
	}

	public static final class CodedException extends RuntimeException {
		// Annotated so serialization takes the hierarchy walk rather than the
		// single-class fast path; the walk must stop at RuntimeException.
		@SurrealName("error_code")
		public String code;

		public CodedException() {
		}
	}

	public static class CredentialBaseProfile {
		public String password;

		public CredentialBaseProfile() {
		}
	}

	public static final class CredentialChildProfile extends CredentialBaseProfile {
		public String name;
		public transient String password;

		public CredentialChildProfile() {
		}
	}

	public static class CountedBaseProfile {
		public String counter;

		public CountedBaseProfile() {
		}
	}

	public static final class CountedChildProfile extends CountedBaseProfile {
		public String name;
		public static String counter;

		public CountedChildProfile() {
		}
	}

	@Test
	void writeIncludesInheritedFields() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final ChildProfile profile = new ChildProfile();
			profile.slug = "abc";
			profile.name = "Tobie";

			final Value value = surreal.query("RETURN $profile", Collections.singletonMap("profile", profile)).take(0);

			assertEquals(Arrays.asList("name", "slug"), keys(value.getObject()));
			final ChildProfile read = value.get(ChildProfile.class);
			assertEquals("abc", read.slug);
			assertEquals("Tobie", read.name);
		}
	}

	@Test
	void roundTripsInheritedFieldsThroughCreateAndSelect() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final ChildProfile profile = new ChildProfile();
			profile.slug = "abc";
			profile.name = "Tobie";

			final ChildProfile created = surreal.create(ChildProfile.class, "child_profile", profile).get(0);

			assertEquals("abc", created.slug);
			assertEquals("Tobie", created.name);
		}
	}

	@Test
	void honorsSurrealNameOnInheritedFields() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final AnnotatedChildProfile profile = new AnnotatedChildProfile();
			profile.createdAt = "2026-06-10";
			profile.name = "Ada";

			final Value value = surreal.query("RETURN $profile", Collections.singletonMap("profile", profile)).take(0);

			assertEquals(Arrays.asList("created_at", "name"), keys(value.getObject()));
			final AnnotatedChildProfile read = value.get(AnnotatedChildProfile.class);
			assertEquals("2026-06-10", read.createdAt);
			assertEquals("Ada", read.name);
		}
	}

	@Test
	void doesNotWriteHiddenSuperclassFields() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final HiddenChildProfile profile = new HiddenChildProfile();
			profile.id = "child";
			((HiddenBaseProfile) profile).id = "hidden base value";

			final Value value = surreal.query("RETURN $profile", Collections.singletonMap("profile", profile)).take(0);

			assertEquals(Collections.singletonList("child_id"), keys(value.getObject()));
			assertEquals("child", value.getObject().get("child_id").getString());
		}
	}

	@Test
	void rejectsDuplicateResolvedNamesAcrossHierarchyDuringSerialization() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final DuplicateChildProfile profile = new DuplicateChildProfile();
			profile.code = "base";
			profile.childCode = "child";

			assertThrows(SurrealException.class,
					() -> surreal.query("RETURN $profile", Collections.singletonMap("profile", profile)));
		}
	}

	@Test
	void stopsHierarchyWalkAtJdkClasses() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final CodedException coded = new CodedException();
			coded.code = "E42";

			final Value value = surreal.query("RETURN $coded", Collections.singletonMap("coded", coded)).take(0);

			// Only the user-declared field is serialized; Throwable internals
			// (detailMessage, stackTrace, ...) stay out.
			assertEquals(Collections.singletonList("error_code"), keys(value.getObject()));
			assertFalse(keys(value.getObject()).contains("detailMessage"));
		}
	}

	@Test
	void transientSubclassFieldHidesSuperclassFieldOnWrite() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final CredentialChildProfile profile = new CredentialChildProfile();
			profile.name = "Ada";
			((CredentialBaseProfile) profile).password = "secret";

			final Value value = surreal.query("RETURN $profile", Collections.singletonMap("profile", profile)).take(0);

			assertEquals(Collections.singletonList("name"), keys(value.getObject()));
		}
	}

	@Test
	void transientSubclassFieldHidesSuperclassFieldOnRead() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Map<String, java.lang.Object> values = new LinkedHashMap<>();
			values.put("name", "Ada");
			values.put("password", "from database");
			final Value value = surreal.query("RETURN $profile", Collections.singletonMap("profile", values)).take(0);

			final CredentialChildProfile read = value.get(CredentialChildProfile.class);
			assertEquals("Ada", read.name);
			assertNull(((CredentialBaseProfile) read).password);
		}
	}

	@Test
	void staticSubclassFieldHidesSuperclassFieldOnWrite() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final CountedChildProfile profile = new CountedChildProfile();
			profile.name = "Ada";
			((CountedBaseProfile) profile).counter = "base value";

			final Value value = surreal.query("RETURN $profile", Collections.singletonMap("profile", profile)).take(0);

			assertEquals(Collections.singletonList("name"), keys(value.getObject()));
		}
	}

	private static Surreal openMemoryDatabase() {
		final Surreal surreal = new Surreal();
		surreal.connect("memory").useNs("inherited_fields_tests").useDb("inherited_fields_tests");
		return surreal;
	}

	private static List<String> keys(final Object object) {
		final List<String> keys = new ArrayList<>();
		for (final Entry entry : object) {
			keys.add(entry.getKey());
		}
		Collections.sort(keys);
		return keys;
	}
}
