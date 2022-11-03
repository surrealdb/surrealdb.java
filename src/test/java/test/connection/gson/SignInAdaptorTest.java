package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.auth.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.*;

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

        SurrealAuthCredentials expected = SurrealNamespaceCredentials.from("namespace_user", "namespace_user_password", "some_namespace");
        SurrealAuthCredentials actual = GsonTestUtils.deserialize(signInJson, SurrealNamespaceCredentials.class);

        assertEquals(expected, actual);
    }

    @Test
    void testDeserializationOfDatabaseUserSignIn() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "database_user");
        signInJson.addProperty("pass", "database_user_password");
        signInJson.addProperty("NS", "some_namespace");
        signInJson.addProperty("DB", "some_database");

        SurrealAuthCredentials expected = SurrealDatabaseCredentials.from("database_user", "database_user_password", "some_namespace", "some_database");
        SurrealAuthCredentials actual = GsonTestUtils.deserialize(signInJson, SurrealDatabaseCredentials.class);

        assertEquals(expected, actual);
    }

    @Test
    void testDeserializationOfScopeUserSignIn() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("NS", "some_namespace");
        signInJson.addProperty("DB", "some_database");
        signInJson.addProperty("SC", "some_scope");

        SurrealAuthCredentials expected = SurrealScopeCredentials.from("some_namespace", "some_database", "some_scope");
        SurrealAuthCredentials actual = GsonTestUtils.deserialize(signInJson, SurrealScopeCredentials.class);

        assertEquals(expected, actual);
    }
}
