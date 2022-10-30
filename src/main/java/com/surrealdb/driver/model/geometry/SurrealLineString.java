package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;
import lombok.With;

@Value
@With
public class SurrealLineString implements SurrealGeometryPrimitive {

    ImmutableList<SurrealPoint> points;

    public SurrealLineString(ImmutableList<SurrealPoint> points) {
        this.points = points;
    }

    public SurrealLineString(SurrealPoint... points) {
        this.points = ImmutableList.copyOf(points);
    }

}
