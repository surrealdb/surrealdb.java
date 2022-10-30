package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;
import lombok.With;

@Value
@With
public class Line implements GeometryPrimitive {

    ImmutableList<Point> points;

    public Line(ImmutableList<Point> points) {
        this.points = points;
    }

    public Line(Point... points) {
        this.points = ImmutableList.copyOf(points);
    }

}
