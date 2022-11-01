package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.model.geometry.Point;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.assertGeometryCoordinatesEqual;
import static test.connection.gson.GsonTestUtils.assertJsonHasPropertyString;

public class GeometryPointAdaptorTest {

    @Test
    @DisplayName("Test serializing a point")
    void testPointWithZeroXYSerialization() {
        Point point = Point.fromXY(0, 0);
        JsonObject serialized = GsonTestUtils.serialize(point).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "type", "Point");
        assertGeometryCoordinatesEqual(0, 0, serialized.get("coordinates"));
    }

    @Test
    @DisplayName("Test serialization precision of point with the coordinates [Double.MAX_VALUE, -Double.MAX_VALUE]")
    void testPointF64Serialization() {
        Point point = Point.fromXY(Double.MAX_VALUE, -Double.MAX_VALUE);
        JsonObject serialized = GsonTestUtils.serialize(point).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "type", "Point");
        assertGeometryCoordinatesEqual(Double.MAX_VALUE, -Double.MAX_VALUE, serialized.get("coordinates"));
    }

    @Test
    @DisplayName("Test serialization precision of a point with lots of decimal places")
    void testPointSerializationPrecision() {
        var expectedX = Math.E;
        var expectedY = Math.PI;

        Point point = Point.fromXY(expectedX, expectedY);
        JsonObject serialized = GsonTestUtils.serialize(point).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "type", "Point");
        assertGeometryCoordinatesEqual(expectedX, expectedY, serialized.get("coordinates"));
    }

    @Test
    @DisplayName("Test deserializing a point")
    void testPointDeserialization() {
        String json = "{\"type\":\"Point\",\"coordinates\":[1.5,2.25]}";
        Point deserialized = GsonTestUtils.deserialize(json, Point.class);

        assertEquals(1.5, deserialized.getX());
        assertEquals(2.25, deserialized.getY());
    }
}
