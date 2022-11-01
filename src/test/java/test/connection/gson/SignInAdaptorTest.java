package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.model.SignIn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.*;

public class SignInAdaptorTest {

    @Test
    void testSerializationOfRootUserSignIn() {
        SignIn signIn = new SignIn("generic_username", "a_password", null, null, null);
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonDoesNotHaveProperties(serialized, "NS", "ns");
        assertJsonDoesNotHaveProperties(serialized, "DB", "db");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");

    }

    @Test
    void testSerializationOfNamespaceUserSignIn() {
        SignIn signIn = new SignIn("generic_username", "a_password", "the_namespace", null, null);
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonDoesNotHaveProperties(serialized, "DB", "db");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");
    }

    @Test
    void testSerializationOfDatabaseUserSignIn() {
        SignIn signIn = new SignIn("generic_username", "a_password", "the_namespace", "database_name", null);
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonHasPropertyString(serialized, "DB", "database_name");
        assertJsonDoesNotHaveProperties(serialized, "SC", "sc");
    }

    @Test
    void testSerializationOfScopeUserSignIn() {
        SignIn signIn = new SignIn("generic_username", "a_password", "the_namespace", "database_name", "auth_scope");
        JsonObject serialized = GsonTestUtils.serialize(signIn).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "user", "generic_username");
        assertJsonHasPropertyString(serialized, "pass", "a_password");
        assertJsonHasPropertyString(serialized, "NS", "the_namespace");
        assertJsonHasPropertyString(serialized, "DB", "database_name");
        assertJsonHasPropertyString(serialized, "SC", "auth_scope");
    }

    @Test
    void testDeserializationOfRootUserSignIn() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "root_user");
        signInJson.addProperty("pass", "root_user_password");

        SignIn expected = new SignIn("root_user", "root_user_password", null, null, null);
        SignIn actual = GsonTestUtils.deserialize(signInJson, SignIn.class);

        assertEquals(expected, actual);
    }

    @Test
    void testDeserializationOfNamespaceUserSignIn() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "namespace_user");
        signInJson.addProperty("pass", "namespace_user_password");
        signInJson.addProperty("NS", "some_namespace");

        SignIn expected = new SignIn("namespace_user", "namespace_user_password", "some_namespace", null, null);
        SignIn actual = GsonTestUtils.deserialize(signInJson, SignIn.class);

        assertEquals(expected, actual);
    }

    @Test
    void testDeserializationOfDatabaseUserSignIn() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "database_user");
        signInJson.addProperty("pass", "database_user_password");
        signInJson.addProperty("NS", "some_namespace");
        signInJson.addProperty("DB", "some_database");

        SignIn expected = new SignIn("database_user", "database_user_password", "some_namespace", "some_database", null);
        SignIn actual = GsonTestUtils.deserialize(signInJson, SignIn.class);

        assertEquals(expected, actual);
    }

    @Test
    void testDeserializationOfScopeUserSignIn() {
        JsonObject signInJson = new JsonObject();
        signInJson.addProperty("user", "scope_user");
        signInJson.addProperty("pass", "scope_user_password");
        signInJson.addProperty("NS", "some_namespace");
        signInJson.addProperty("DB", "some_database");
        signInJson.addProperty("SC", "some_scope");

        SignIn expected = new SignIn("scope_user", "scope_user_password", "some_namespace", "some_database", "some_scope");
        SignIn actual = GsonTestUtils.deserialize(signInJson, SignIn.class);

        assertEquals(expected, actual);
    }
}
