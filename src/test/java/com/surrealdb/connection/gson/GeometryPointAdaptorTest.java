package com.surrealdb.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.geometry.Point;
import com.surrealdb.meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GsonTestUtils.assertGeometryCoordinatesEqual;
import static com.surrealdb.meta.utils.GsonTestUtils.assertJsonHasPropertyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeometryPointAdaptorTest {

    @Test
    void testSerializationPrecision() {
        double expectedX = Math.E;
        double expectedY = Math.PI;

        Point point = Point.fromXY(expectedX, expectedY);
        JsonObject serialized = GsonTestUtils.serialize(point).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "type", "Point");
        assertGeometryCoordinatesEqual(expectedX, expectedY, serialized.get("coordinates"));
    }

    @Test
    void testSerializationMaxMinValues() {
        double expectedX = Double.MAX_VALUE;
        double expectedY = -Double.MAX_VALUE;

        Point point = Point.fromXY(expectedX, expectedY);
        JsonObject serialized = GsonTestUtils.serialize(point).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "type", "Point");
        assertGeometryCoordinatesEqual(expectedX, expectedY, serialized.get("coordinates"));
    }

    @Test
    void testDeserialization() {
        String json = "{\"type\":\"Point\",\"coordinates\":[1.5,2.25]}";
        Point deserialized = GsonTestUtils.deserialize(json, Point.class);

        assertEquals(1.5, deserialized.getX());
        assertEquals(2.25, deserialized.getY());
    }
}
