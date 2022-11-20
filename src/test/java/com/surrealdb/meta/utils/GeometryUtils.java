package com.surrealdb.meta.utils;

import com.surrealdb.driver.geometry.LineString;
import com.surrealdb.driver.geometry.LinearRing;
import com.surrealdb.driver.geometry.Polygon;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GeometryUtils {

    public static LinearRing createQuadLinearRing(boolean autoClose) {
        LineString.Builder builder = LinearRing.builder()
            .addPointXY(-1, -1)
            .addPointXY(-1, 1)
            .addPointXY(1, 1)
            .addPointXY(1, -1);

        if (!autoClose) {
            builder.addPointXY(-1, -1);
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
        LinearRing interior = createQuadLinearRing(false).scale(0.75, 0.75);

        return Polygon.builder()
            .setExterior(exterior)
            .addInterior(interior)
            .build();
    }
}
