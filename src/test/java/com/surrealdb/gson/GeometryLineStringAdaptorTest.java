package com.surrealdb.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surrealdb.geometry.LineString;
import com.surrealdb.geometry.Point;
import meta.tests.GsonAdaptorTest;
import meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static meta.utils.GsonTestUtils.assertGeometryCoordinatesEqual;
import static meta.utils.GsonTestUtils.assertJsonHasPropertyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GeometryLineStringAdaptorTest extends GsonAdaptorTest {

    @Test
    @Override
    protected void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation() {
        LineString lineString = LineString.from(
            Point.fromXY(1.5, 2.25),
            Point.fromXY(3.75, 4.125)
        );

        JsonObject serialized = GsonTestUtils.serialize(lineString).getAsJsonObject();
        JsonArray coordinates = serialized.get("coordinates").getAsJsonArray();

        assertJsonHasPropertyString(serialized, "type", "LineString");

        assertEquals(2, coordinates.size());
        assertGeometryCoordinatesEqual(1.5, 2.25, coordinates.get(0));
        assertGeometryCoordinatesEqual(3.75, 4.125, coordinates.get(1));
    }

    @Test
    @Override
    protected void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "LineString");
        JsonArray coordinates = new JsonArray();
        coordinates.add(GsonTestUtils.createJsonArray(32, 54));
        coordinates.add(GsonTestUtils.createJsonArray(-11, -5));
        json.add("coordinates", coordinates);

        LineString deserialized = GsonTestUtils.deserialize(json, LineString.class);

        assertEquals(2, deserialized.getPointCount());
        assertEquals(Point.fromXY(32, 54), deserialized.getPoint(0));
        assertEquals(Point.fromXY(-11, -5), deserialized.getPoint(1));
    }
}