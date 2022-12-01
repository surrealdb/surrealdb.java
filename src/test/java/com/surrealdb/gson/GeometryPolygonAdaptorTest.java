package com.surrealdb.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surrealdb.geometry.Polygon;
import meta.tests.GsonAdaptorTest;
import meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static meta.utils.GeometryUtils.createQuadPolygonWithHole;
import static meta.utils.GsonTestUtils.assertGeometryCoordinatesEqual;
import static meta.utils.GsonTestUtils.serialize;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeometryPolygonAdaptorTest extends GsonAdaptorTest {


    @Test
    @Override
    protected void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation() {
        Polygon quadPolygonWithHole = createQuadPolygonWithHole();

        JsonObject serialized = serialize(quadPolygonWithHole).getAsJsonObject();
        JsonArray coordinates = serialized.get("coordinates").getAsJsonArray();

        assertEquals("Polygon", serialized.get("type").getAsString());
        assertEquals(2, coordinates.size());

        JsonArray exterior = coordinates.get(0).getAsJsonArray();
        assertEquals(5, exterior.size());
        assertGeometryCoordinatesEqual(-1, -1, exterior.get(0));
        assertGeometryCoordinatesEqual(1, -1, exterior.get(1));
        assertGeometryCoordinatesEqual(1, 1, exterior.get(2));
        assertGeometryCoordinatesEqual(-1, 1, exterior.get(3));
        assertGeometryCoordinatesEqual(-1, -1, exterior.get(4));

        JsonArray interior = coordinates.get(1).getAsJsonArray();
        assertEquals(5, interior.size());
        assertGeometryCoordinatesEqual(-0.75, -0.75, interior.get(0));
        assertGeometryCoordinatesEqual(-0.75, 0.75, interior.get(1));
        assertGeometryCoordinatesEqual(0.75, 0.75, interior.get(2));
        assertGeometryCoordinatesEqual(0.75, -0.75, interior.get(3));
        assertGeometryCoordinatesEqual(-0.75, -0.75, interior.get(4));
    }

    @Test
    @Override
    protected void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject() {
        // Cheap way to test this is to serialize a Polygon and then deserialize it.
        // Should be fine since we already the serialization.
        Polygon quadPolygonWithHole = createQuadPolygonWithHole();

        JsonObject serialized = serialize(quadPolygonWithHole).getAsJsonObject();
        Polygon deserialized = GsonTestUtils.deserialize(serialized, Polygon.class);

        assertEquals(quadPolygonWithHole, deserialized);
    }
}
