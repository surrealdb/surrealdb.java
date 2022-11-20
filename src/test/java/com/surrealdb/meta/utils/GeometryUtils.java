package com.surrealdb.meta.utils;

import com.surrealdb.driver.geometry.LineString;
import com.surrealdb.driver.geometry.LinearRing;
import com.surrealdb.driver.geometry.Polygon;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GeometryUtils {

    public static LinearRing createQuadLinearRing(boolean autoClose) {
        LineString.Builder builder = LinearRing.builder()
            .addPointXY(0, 0)
            .addPointXY(0, 1)
            .addPointXY(1, 1)
            .addPointXY(1, 0);

        if (!autoClose) {
            builder.addPointXY(0, 0);
        }

        return builder.buildLinearRing();
    }

    public static Polygon createQuadPolygon(boolean autoClose) {
        LinearRing exterior = createQuadLinearRing(autoClose);

        return Polygon.builder()
            .setExterior(exterior)
            .build();
    }

    public static Polygon createQuadPolygonWithHole() {
        LineString exterior = createQuadLinearRing(true);

        LineString interior = LinearRing.builder()
            .addPointXY(0.1, 0.1)
            .addPointXY(0.1, 0.9)
            .addPointXY(0.9, 0.9)
            .addPointXY(0.9, 0.1)
            .build();

        return Polygon.builder()
            .setExterior(exterior)
            .addInterior(interior)
            .build();
    }
}
