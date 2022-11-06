package test.connection.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surrealdb.driver.geometry.GeometryCollection;
import com.surrealdb.driver.geometry.MultiPoint;
import com.surrealdb.driver.geometry.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.assertGeometryCoordinatesEqual;
import static test.connection.gson.GsonTestUtils.assertJsonHasPropertyString;

public class GeometryCollectionSerializationTest {

    @Test
    void testGeometryCollectionSerialization() {
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
}
