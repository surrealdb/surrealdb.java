package com.surrealdb.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surrealdb.geometry.LinearRing;
import com.surrealdb.geometry.Point;
import meta.tests.GsonAdaptorTest;
import meta.utils.GeometryUtils;
import meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static meta.utils.GsonTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeometryLinearRingTest extends GsonAdaptorTest {

    @Test
    @Override
    protected void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation() {
        LinearRing linearRing = GeometryUtils.createQuadLinearRing(true, true);

        JsonObject serialized = GsonTestUtils.serialize(linearRing).getAsJsonObject();
        JsonArray coordinates = serialized.get("coordinates").getAsJsonArray();

        // LinearRing is a LineString in the GeoJSON spec, so it should have a type of "LineString"
        assertJsonHasPropertyString(serialized, "type", "LineString");

        assertEquals(5, coordinates.size());
        assertGeometryCoordinatesEqual(-1, -1, coordinates.get(0));
        assertGeometryCoordinatesEqual(1, -1, coordinates.get(1));
        assertGeometryCoordinatesEqual(1, 1, coordinates.get(2));
        assertGeometryCoordinatesEqual(-1, 1, coordinates.get(3));
        assertGeometryCoordinatesEqual(-1, -1, coordinates.get(4));
    }

    @Test
    @Override
    protected void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "LineString");
        JsonArray coordinates = new JsonArray();
        coordinates.add(createJsonArray(-1, -1));
        coordinates.add(createJsonArray(1, -1));
        coordinates.add(createJsonArray(1, 1));
        coordinates.add(createJsonArray(-1, 1));
        coordinates.add(createJsonArray(-1, -1));
        json.add("coordinates", coordinates);

        LinearRing deserialized = GsonTestUtils.deserialize(json, LinearRing.class);

        assertEquals(5, deserialized.getPointCount());
        GeometryUtils.assertPointEquals(Point.fromXY(-1, -1), deserialized.getPoint(0));
        GeometryUtils.assertPointEquals(Point.fromXY(1, -1), deserialized.getPoint(1));
        GeometryUtils.assertPointEquals(Point.fromXY(1, 1), deserialized.getPoint(2));
        GeometryUtils.assertPointEquals(Point.fromXY(-1, 1), deserialized.getPoint(3));
        GeometryUtils.assertPointEquals(Point.fromXY(-1, -1), deserialized.getPoint(4));
    }
}
