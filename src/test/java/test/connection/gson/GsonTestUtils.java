package test.connection.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.connection.gson.SurrealGsonUtils;
import lombok.experimental.UtilityClass;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public class GsonTestUtils {

    private static final Gson gsonInstance = SurrealGsonUtils.makeGsonInstanceSurrealCompatible(new Gson());

    public static <T> JsonElement serializeToJsonElement(T object) {
        return gsonInstance.toJsonTree(object);
    }

    public static <T> T deserializeFromJsonElement(JsonElement jsonElement, Class<T> clazz) {
        return gsonInstance.fromJson(jsonElement, clazz);
    }

    public static void assertJsonElementEquals(JsonElement expected, JsonElement actual) {
        assertEquals(expected, actual);
    }

    public static void assertJsonHasPropertyString(JsonObject serialized, String property, String value) {
        assertTrue(serialized.has(property), () -> "Serialized object does not have property " + property);
        assertEquals(value, serialized.get(property).getAsString(), () -> "Serialized object does not have property " + property + " with value " + value);
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
