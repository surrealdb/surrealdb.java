package test.connection.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.connection.gson.SurrealGsonUtils;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public class GsonTestUtils {

    private static final Gson gsonInstance = SurrealGsonUtils.makeGsonInstanceSurrealCompatible(new Gson());

    public static <T> JsonElement serializeToJsonElement(T object) {
        return gsonInstance.toJsonTree(object);
    }

    public static <T> JsonElement serializeToJsonElement(T object, Type genericType) {
        return gsonInstance.toJsonTree(object, genericType);
    }

    public static <T> T deserializeFromJsonElement(JsonElement jsonElement, Type type) {
        return gsonInstance.fromJson(jsonElement, type);
    }

    public static void assertJsonElementEquals(JsonElement expected, JsonElement actual) {
        assertEquals(expected, actual);
    }

    public static void assertJsonHasPropertyString(JsonObject serialized, String property, String expected) {
        assertTrue(serialized.has(property), () -> "Serialized object does not have property " + property);
        String actual = serialized.get(property).getAsString();
        assertEquals(expected, actual, () -> "Serialized object does not have property " + property + " with expected value " + expected);
    }

    public static void assertJsonHasPropertyInt(JsonObject serialized, String property, int expected) {
        assertTrue(serialized.has(property), () -> "Serialized object does not have property " + property);
        int actual = serialized.get(property).getAsInt();
        assertEquals(expected, actual, () -> "Serialized object does not have property " + property + " with expected value " + expected);
    }

    public static void assertJsonDoesNotHaveProperty(JsonObject serialized, String property) {
        assertFalse(serialized.has(property), () -> "Serialized object has property " + property);
    }

    public static void assertJsonDoesNotHaveProperties(JsonObject serialized, String... properties) {
        for (String property : properties) {
            assertJsonDoesNotHaveProperty(serialized, property);
        }
    }
}
