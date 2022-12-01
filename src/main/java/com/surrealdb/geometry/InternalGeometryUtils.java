package com.surrealdb.geometry;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

@UtilityClass
class InternalGeometryUtils {

    private static final @NotNull DecimalFormat POINT_FORMATTER = new DecimalFormat("#.################");

    static @NotNull String calculateWktGeneric(@NotNull String geometryType, @NotNull List<String> args) {
        if (args.isEmpty()) {
            return geometryType + " EMPTY";
        }

        return geometryType + " (" + String.join(", ", args) + ")";
    }

    static @NotNull String calculateWktGeometryRepresentationPoints(@NotNull String geometryType, @NotNull Iterable<? extends Point> pointIterable) {
        Iterator<? extends Point> pointsIterator = pointIterable.iterator();

        if (!pointsIterator.hasNext()) {
            return geometryType + " EMPTY";
        }

        return geometryType + " " + calculateWktPointsPrimitive(pointsIterator);
    }

    static @NotNull String calculateWktPoint(@NotNull Point point) {
        return "POINT (" + calculateWktPointPrimitive(point) + ")";
    }

    static @NotNull String calculateWktPointsPrimitive(@NotNull Iterator<? extends Point> points) {
        StringBuilder builder = new StringBuilder();

        builder.append("(");

        while (points.hasNext()) {
            Point point = points.next();
            String stringifiedPoint = calculateWktPointPrimitive(point);
            builder.append(stringifiedPoint);

            if (points.hasNext()) {
                builder.append(", ");
            }
        }

        builder.append(")");

        return builder.toString();
    }

    static @NotNull String calculateWktPointPrimitive(@NotNull Point point) {
        String formattedX = POINT_FORMATTER.format(point.getX());
        String formattedY = POINT_FORMATTER.format(point.getY());

        return formattedX + " " + formattedY;
    }

    static int calculatePointCountOfGeometries(@NotNull Iterable<? extends GeometryPrimitive> geometries) {
        int pointCount = 0;

        for (Geometry geometry : geometries) {
            pointCount += geometry.getPointCount();
        }

        return pointCount;
    }

    /**
     * <b>CAREFUL: </b> This method could cause a recursive stack overflow if the geometries provided use
     * this method to calculate their center.
     *
     * @param geometries The geometries to calculate the center of.
     * @return The center of the geometries.
     */
    static @NotNull Point calculateCenterOfGeometries(@NotNull List<? extends GeometryPrimitive> geometries) {
        double x = 0;
        double y = 0;
        int count = 0;

        for (GeometryPrimitive geometry : geometries) {
            Point geometryCenter = geometry.getCenter();

            x += geometryCenter.getX();
            y += geometryCenter.getY();
            count++;
        }

        return Point.fromXY(x / count, y / count);
    }

    static @NotNull Point calculateCenterOfPointsIterable(@NotNull Iterable<? extends Point> iterable) {
        return calculateCenterOfPointsIterator(iterable.iterator());
    }

    static @NotNull Point calculateCenterOfPointsIterator(@NotNull Iterator<? extends Point> iterator) {
        double x = 0;
        double y = 0;
        int count = 0;

        while (iterator.hasNext()) {
            Point point = iterator.next();

            x += point.getX();
            y += point.getY();
            ++count;
        }

        return Point.fromXY(x / count, y / count);
    }
}