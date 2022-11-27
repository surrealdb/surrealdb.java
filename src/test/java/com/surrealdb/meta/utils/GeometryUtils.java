package com.surrealdb.meta.utils;

import com.surrealdb.driver.geometry.LinearRing;
import com.surrealdb.driver.geometry.Point;
import com.surrealdb.driver.geometry.Polygon;
import lombok.experimental.UtilityClass;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UtilityClass
public class GeometryUtils {

    private static final double POINT_EQUALS_PRECISION = 0.00000001;

    public static LinearRing createQuadLinearRing(boolean autoClose) {
        LinearRing.Builder builder = LinearRing.builder()
            .addPointXY(-1, -1)
            .addPointXY(-1, 1)
            .addPointXY(1, 1)
            .addPointXY(1, -1);

        if (!autoClose) {
            builder.addPointXY(-1, -1);
        }

        return builder.build();
    }

    public static Polygon createQuadPolygon(boolean autoClose) {
        LinearRing exterior = createQuadLinearRing(autoClose);

        return Polygon.builder()
            .setExterior(exterior)
            .build();
    }

    public static Polygon createQuadPolygonWithHole() {
        LinearRing exterior = createQuadLinearRing(true);
        LinearRing interior = createQuadLinearRing(false).scale(0.75, 0.75);

        return Polygon.builder()
            .setExterior(exterior)
            .addInterior(interior)
            .build();
    }

    public static LinearRing createCircleLinearRing(int vertexCount, double radius) {
        LinearRing.Builder linearRingBuilder = LinearRing.builder();

        double angle = 0;
        double angleIncrement = 2 * Math.PI / vertexCount;

        for (int i = 0; i < vertexCount; i++) {
            linearRingBuilder.addPointXY(radius * Math.cos(angle), radius * Math.sin(angle));
            angle += angleIncrement;
        }

        return linearRingBuilder.build();
    }

    public static Polygon createCirclePolygon(int vertexCount, double radius) {
        LinearRing exterior = createCircleLinearRing(vertexCount, radius);
        return Polygon.from(exterior);
    }

    public static void assertPointEquals(Point expected, Point actual) {
        assertPointEquals(expected, actual, "Point");
    }

    public static void assertPointEquals(Point expected, Point actual, String message) {
        assertEquals(expected.getX(), actual.getX(), POINT_EQUALS_PRECISION, () -> message + " (x)");
        assertEquals(expected.getY(), actual.getY(), POINT_EQUALS_PRECISION, () -> message + " (y)");
    }
}
