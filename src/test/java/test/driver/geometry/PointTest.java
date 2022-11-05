package test.driver.geometry;

import com.surrealdb.driver.geometry.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointTest {

    @Test
    void testFromXY() {
        Point point = Point.fromXY(3, 5);

        assertEquals(3, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void testFromYX() {
        Point point = Point.fromYX(5, 3);

        assertEquals(3, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void testWithX() {
        Point point = Point.fromXY(3, 5).withX(7);

        assertEquals(7, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void testWithY() {
        Point point = Point.fromXY(3, 5).withY(7);

        assertEquals(3, point.getX());
        assertEquals(7, point.getY());
    }

    @Test
    void testEquality() {
        Point point1 = Point.fromXY(23, 73);
        Point point2 = Point.fromXY(23, 73);

        assertEquals(point1, point2);
        assertEquals(point1.hashCode(), point2.hashCode());
    }
}
