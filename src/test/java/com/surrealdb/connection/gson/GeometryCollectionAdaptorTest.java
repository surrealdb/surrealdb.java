package com.surrealdb.connection.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.geometry.*;
import com.surrealdb.meta.GsonAdaptorTest;
import com.surrealdb.meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GsonTestUtils.assertGeometryCoordinatesEqual;
import static com.surrealdb.meta.utils.GsonTestUtils.assertJsonHasPropertyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeometryCollectionAdaptorTest extends GsonAdaptorTest {

    @Test
    @Override
    protected void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation() {
        GeometryCollection geometryCollection = GeometryCollection.builder()
            .addGeometry(Point.fromXY(1.5, 2.25))
            .addGeometry(MultiPoint.from(Point.fromXY(10, 12), Point.fromXY(14, 16)))
            .build();

        JsonObject json = GsonTestUtils.serialize(geometryCollection).getAsJsonObject();

        assertJsonHasPropertyString(json, "type", "GeometryCollection");

        JsonArray geometries = json.get("geometries").getAsJsonArray();
        assertEquals(2, geometries.size());

        JsonObject point = geometries.get(0).getAsJsonObject();
        assertJsonHasPropertyString(point, "type", "Point");
        assertGeometryCoordinatesEqual(1.5, 2.25, point.get("coordinates"));

        JsonObject multiPoint = geometries.get(1).getAsJsonObject();
        assertJsonHasPropertyString(multiPoint, "type", "MultiPoint");
        JsonArray multiPointCoordinates = multiPoint.get("coordinates").getAsJsonArray();
        assertEquals(2, multiPointCoordinates.size());
        assertGeometryCoordinatesEqual(10, 12, multiPointCoordinates.get(0));
        assertGeometryCoordinatesEqual(14, 16, multiPointCoordinates.get(1));
    }

    @Test
    @Override
    protected void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject() {
        GeometryCollection expected = GeometryCollection.builder()
            .addGeometry(Point.fromXY(1.5, 2.25))
            .addGeometry(MultiPoint.from(Point.fromXY(10, 12), Point.fromXY(14, 16)))
            .addGeometry(LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4)))
            .addGeometry(Polygon.from(LinearRing.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6), Point.fromXY(1, 2))))
            .addGeometry(MultiPolygon.from(
                Polygon.from(LinearRing.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6), Point.fromXY(1, 2))),
                Polygon.from(LinearRing.from(Point.fromXY(7, 8), Point.fromXY(9, 10), Point.fromXY(11, 12), Point.fromXY(7, 8)))
            ))
            .build();

        JsonElement serialized = GsonTestUtils.serialize(expected);
        assertEquals(expected, GsonTestUtils.deserialize(serialized, GeometryCollection.class));
    }
}
