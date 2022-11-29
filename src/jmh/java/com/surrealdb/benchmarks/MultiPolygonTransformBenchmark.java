package com.surrealdb.benchmarks;

import com.surrealdb.geometry.LinearRing;
import com.surrealdb.geometry.MultiPolygon;
import com.surrealdb.geometry.Point;
import com.surrealdb.geometry.Polygon;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("unused")
@State(Scope.Benchmark)
@Fork(value = 1)
public class MultiPolygonTransformBenchmark {

    private final MultiPolygon multiPolygon = createMultiPolygon();

    private LinearRing createCircularLinearRing(int numPoints, double radius) {
        List<Point> points = new ArrayList<>(numPoints);
        for (int i = 0; i < numPoints; i++) {
            double angle = 2 * Math.PI * i / numPoints;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            points.add(Point.fromXY(x, y));
        }

        return LinearRing.from(points);
    }

    private Polygon createCircle(int vertexCount, double radius) {
        return Polygon.builder()
            .setExterior(createCircularLinearRing(vertexCount, radius))
            .addInterior(createCircularLinearRing(vertexCount, radius / 2))
            .build();
    }

    private MultiPolygon createMultiPolygon() {
        MultiPolygon multiPolygon = MultiPolygon.builder()
            .addPolygon(createCircle(1024, 20))
            .addPolygon(createCircle(360, 10).translate(-50, 0))
            .addPolygon(createCircle(360, 10).translate(50, 0))
            .addPolygon(createCircle(360, 10).translate(0, -50))
            .addPolygon(createCircle(360, 10).translate(0, 50))
            .build();

        System.out.println("Point count: " + multiPolygon.getPointCount());

        return multiPolygon;
    }

    @Benchmark
    @Warmup(iterations = 12, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 5, time = 1000, timeUnit = MILLISECONDS)
    @OutputTimeUnit(MILLISECONDS)
    public void translate(Blackhole blackhole) {
        blackhole.consume(multiPolygon.translate(10, 10));
    }

    @Benchmark
    @Warmup(iterations = 12, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 5, time = 1000, timeUnit = MILLISECONDS)
    @OutputTimeUnit(MILLISECONDS)
    public void rotate(Blackhole blackhole) {
        blackhole.consume(multiPolygon.rotate(10));
    }

    @Benchmark
    @Warmup(iterations = 12, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 5, time = 1000, timeUnit = MILLISECONDS)
    @OutputTimeUnit(MILLISECONDS)
    public void scale(Blackhole blackhole) {
        blackhole.consume(multiPolygon.scale(10, 10));
    }

    @Benchmark
    @Warmup(iterations = 12, time = 500, timeUnit = MILLISECONDS)
    @Measurement(iterations = 5, time = 1000, timeUnit = MILLISECONDS)
    @OutputTimeUnit(MILLISECONDS)
    public void transform(Blackhole blackhole) {
        Function<Point, Point> pointTransform = point -> Point.fromXY(point.getX() * 50, point.getY() * 50);
        Function<LinearRing, LinearRing> linearRingTransform = linearRing -> linearRing.transform(pointTransform);
        Function<Polygon, Polygon> polygonTransform = polygon -> polygon.transform(linearRingTransform);

        blackhole.consume(multiPolygon.transform(polygonTransform));
    }
}
