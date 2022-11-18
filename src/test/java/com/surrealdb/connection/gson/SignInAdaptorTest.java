package com.surrealdb.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.auth.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.surrealdb.connection.gson.GsonTestUtils.assertJsonDoesNotHaveProperties;
import static com.surrealdb.connection.gson.GsonTestUtils.assertJsonHasPropertyString;

public class SignInAdaptorTest {

    @Test
    void testSerializationOfRootUserSignIn() {
        SurrealAuthCredentials signIn = SurrealRootCredentials.from("generic_username", "a_password");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonDoesNotHaveProperties(serialized, "NS", "ns");
        assertJsonDoesNotHaveProperties(serialized, "DB", "db");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");

    }

    @Test
    void testSerializationOfNamespaceUserSignIn() {
        SurrealAuthCredentials signIn = SurrealNamespaceCredentials.from("generic_username", "a_password", "the_namespace");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonDoesNotHaveProperties(serialized, "DB", "db");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");
    }

    @Test
    void testSerializationOfDatabaseUserSignIn() {
        SurrealAuthCredentials signIn = SurrealDatabaseCredentials.from("generic_username", "a_password", "the_namespace", "database_name");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonHasPropertyString(serialized, "DB", "database_name");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");
    }

    @Test
    void testSerializationOfScopeUserSignIn() {
        SurrealAuthCredentials signIn = SurrealScopeCredentials.from("the_namespace", "database_name", "auth_scope");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonDoesNotHaveProperties(serialized, "user");
        assertJsonDoesNotHaveProperties(serialized, "pass");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonHasPropertyString(serialized, "DB", "database_name");
        assertJsonHasPropertyString(serialized, "SC", "auth_scope");
    }

    @Test
    void testDeserializationOfRootUserSignIn() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "root_user");
        signInJson.addProperty("pass", "root_user_password");

        SurrealAuthCredentials expected = SurrealRootCredentials.from("root_user", "root_user_password");
        SurrealAuthCredentials actual = GsonTestUtils.deserialize(signInJson, SurrealRootCredentials.class);

        assertEquals(expected, actual);
    }

    @Test
    void testDeserializationOfNamespaceUserSignIn() {
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
    void testDeserializationOfDatabaseUserSignIn() {
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
    void testDeserializationOfScopeUserSignIn() {
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
