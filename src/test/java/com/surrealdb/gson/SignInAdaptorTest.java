package com.surrealdb.gson;

import com.google.gson.JsonObject;
import com.surrealdb.auth.*;
import meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static meta.utils.GsonTestUtils.assertJsonDoesNotHaveProperties;
import static meta.utils.GsonTestUtils.assertJsonHasPropertyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SignInAdaptorTest {

    @Test
    void gson_toJson_whenProvidedWithRootUserCredentials_returnsSerializedJson() {
        SurrealAuthCredentials signIn = SurrealRootCredentials.from("generic_username", "a_password");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonDoesNotHaveProperties(serialized, "NS", "ns");
        assertJsonDoesNotHaveProperties(serialized, "DB", "db");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");

    }

    @Test
    void gson_toJson_whenProvidedWithNamespaceUserCredentials_returnsSerializedJson() {
        SurrealAuthCredentials signIn = SurrealNamespaceCredentials.from("generic_username", "a_password", "the_namespace");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonDoesNotHaveProperties(serialized, "DB", "db");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");
    }

    @Test
    void gson_toJson_whenProvidedWithDatabaseUserCredentials_returnsSerializedJson() {
        SurrealAuthCredentials signIn = SurrealDatabaseCredentials.from("generic_username", "a_password", "the_namespace", "database_name");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonHasPropertyString(serialized, "DB", "database_name");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");
    }

    @Test
    void gson_toJson_whenProvidedWithScopeUserCredentials_returnsSerializedJson() {
        SurrealAuthCredentials signIn = SurrealScopeCredentials.from("the_namespace", "database_name", "auth_scope");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonDoesNotHaveProperties(serialized, "user");
        assertJsonDoesNotHaveProperties(serialized, "pass");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonHasPropertyString(serialized, "DB", "database_name");
        assertJsonHasPropertyString(serialized, "SC", "auth_scope");
    }

    @Test
    void gson_fromJson_whenGivenSerializedRootUserCredentials_returnsRootUserCredentialsObject() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "root_user");
        signInJson.addProperty("pass", "root_user_password");

        SurrealAuthCredentials expected = SurrealRootCredentials.from("root_user", "root_user_password");
        SurrealAuthCredentials actual = GsonTestUtils.deserialize(signInJson, SurrealRootCredentials.class);

        assertEquals(expected, actual);
    }

    @Test
    void gson_fromJson_whenGivenSerializedNamespaceUserCredentials_returnsNamespaceUserCredentialsObject() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "namespace_user");
        signInJson.addProperty("pass", "namespace_user_password");
        signInJson.addProperty("NS", "some_namespace");

        SurrealNamespaceCredentials deserialized = GsonTestUtils.deserialize(signInJson, SurrealNamespaceCredentials.class);

        assertEquals("namespace_user", deserialized.getUser());
        assertEquals("namespace_user_password", deserialized.getPassword());
        assertEquals("some_namespace", deserialized.getNamespace());
    }

    @Test
    void gson_fromJson_whenGivenSerializedDatabaseUserCredentials_returnsDatabaseUserCredentialsObject() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "database_user");
        signInJson.addProperty("pass", "database_user_password");
        signInJson.addProperty("NS", "some_namespace");
        signInJson.addProperty("DB", "some_database");

        SurrealDatabaseCredentials deserialized = GsonTestUtils.deserialize(signInJson, SurrealDatabaseCredentials.class);

        assertEquals("database_user", deserialized.getUser());
        assertEquals("database_user_password", deserialized.getPassword());
        assertEquals("some_namespace", deserialized.getNamespace());
        assertEquals("some_database", deserialized.getDatabase());
    }

    @Test
    void gson_fromJson_whenGivenSerializeScopeUserCredentials_returnsScopeUserCredentialsObject() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("NS", "some_namespace");
        signInJson.addProperty("DB", "some_database");
        signInJson.addProperty("SC", "some_scope");

        SurrealScopeCredentials deserialized = GsonTestUtils.deserialize(signInJson, SurrealScopeCredentials.class);

        assertEquals("some_namespace", deserialized.getNamespace());
        assertEquals("some_database", deserialized.getDatabase());
        assertEquals("some_scope", deserialized.getScope());
    }
}
