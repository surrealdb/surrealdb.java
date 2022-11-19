package com.surrealdb.driver.geometry;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

@UtilityClass
class InternalGeometryUtils {

    private static final DecimalFormat POINT_FORMATTER = new DecimalFormat("#.###########");

    static @NotNull String calculateWktGeneric(@NotNull String geometryType, @NotNull List<String> args) {
        if (args.isEmpty()) {
            return geometryType + " EMPTY";
        }

        return geometryType + " (" + String.join(", ", args) + ")";
    }

    static @NotNull String calculateWktGeometryRepresentationPoints(@NotNull String geometryType, @NotNull Iterator<Point> pointIterator) {
        if (!pointIterator.hasNext()) {
            return geometryType + " EMPTY";
        }

        return geometryType + " " + calculateWktPointsPrimitive(pointIterator, true);
    }

    static @NotNull String calculateWktPoint(@NotNull String geometryType, @NotNull Point point) {
        return geometryType + " (" + calculateWktPointPrimitive(point) + ")";
    }

    static @NotNull String calculateWktPointsPrimitive(@NotNull Iterator<Point> points, boolean includeParentheses) {
        StringBuilder builder = new StringBuilder();

        if (includeParentheses) {
            builder.append("(");
        }

        while (points.hasNext()) {
            Point point = points.next();
            String stringifiedPoint = calculateWktPointPrimitive(point);
            builder.append(stringifiedPoint);

            if (points.hasNext()) {
                builder.append(", ");
            }
        }

        if (includeParentheses) {
            builder.append(")");
        }

        return builder.toString();
    }

    static @NotNull String calculateWktPointPrimitive(@NotNull Point point) {
        String formattedX = POINT_FORMATTER.format(point.getX());
        String formattedY = POINT_FORMATTER.format(point.getY());

        return formattedX + " " + formattedY;
    }
}
