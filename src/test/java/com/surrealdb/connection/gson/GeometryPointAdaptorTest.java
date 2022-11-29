package com.surrealdb.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.geometry.Point;
import com.surrealdb.meta.GsonAdaptorTest;
import com.surrealdb.meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GsonTestUtils.assertGeometryCoordinatesEqual;
import static com.surrealdb.meta.utils.GsonTestUtils.assertJsonHasPropertyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeometryPointAdaptorTest extends GsonAdaptorTest {

    @Test
    @Override
    protected void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation() {
        double expectedX = Math.E;
        double expectedY = Math.PI;

        Point point = Point.fromXY(expectedX, expectedY);
        JsonObject serialized = GsonTestUtils.serialize(point).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "type", "Point");
        assertGeometryCoordinatesEqual(expectedX, expectedY, serialized.get("coordinates"));
    }

    @Test
    @Override
    protected void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject() {
        String json = "{\"type\":\"Point\",\"coordinates\":[1.5,2.25]}";
        Point deserialized = GsonTestUtils.deserialize(json, Point.class);

        assertEquals(1.5, deserialized.getX());
        assertEquals(2.25, deserialized.getY());
    }
}
