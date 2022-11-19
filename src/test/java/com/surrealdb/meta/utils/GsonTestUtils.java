package com.surrealdb.meta.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.connection.gson.SurrealGsonUtils;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

@UtilityClass
public class GsonTestUtils {

    private static final Gson gsonInstance = SurrealGsonUtils.makeGsonInstanceSurrealCompatible(new Gson());

    public static <T> JsonElement serialize(T object) {
        return gsonInstance.toJsonTree(object);
    }

    public static <T> JsonElement serialize(T object, Type genericType) {
        return gsonInstance.toJsonTree(object, genericType);
    }

    public static <T> T deserialize(JsonElement json, Type type) {
        return gsonInstance.fromJson(json, type);
    }

    public static <T> T deserialize(String json, Type type) {
        return gsonInstance.fromJson(json, type);
    }

    public static void assertJsonHasPropertyString(JsonObject serialized, String property, String expected) {
        assertTrue(serialized.has(property), () -> "Serialized object must have property " + property);
        String actual = serialized.get(property).getAsString();
        assertEquals(expected, actual, () -> "Serialized object does must have property " + property + " with expected value " + expected);
    }

    public static void assertJsonHasPropertyInt(JsonObject serialized, String property, int expected) {
        assertTrue(serialized.has(property), () -> "Serialized object must have property " + property);
        int actual = serialized.get(property).getAsInt();
        assertEquals(expected, actual, () -> "Serialized object must have property " + property + " with expected value " + expected);
    }

    public static void assertJsonDoesNotHaveProperty(JsonObject serialized, String property) {
        assertFalse(serialized.has(property), () -> "Serialized object must not have property " + property);
    }

    public static void assertJsonDoesNotHaveProperties(JsonObject serialized, String... properties) {
        for (String property : properties) {
            assertJsonDoesNotHaveProperty(serialized, property);
        }
    }

    public static void assertGeometryCoordinatesEqual(double expectedX, double expectedY, JsonElement actual) {
        assertEquals(2, actual.getAsJsonArray().size(), "Check coordinate array size");
        assertEquals(expectedX, actual.getAsJsonArray().get(0).getAsDouble(), "Check x coordinate");
        assertEquals(expectedY, actual.getAsJsonArray().get(1).getAsDouble(), "Check y coordinate");
    }

    public static JsonElement createJsonArray(double... values) {
        JsonArray array = new JsonArray();
        for (double value : values) {
            array.add(value);
        }
        return array;
    }
}
