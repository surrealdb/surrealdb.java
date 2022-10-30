package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;
import lombok.With;

@Value
@With
public class MultiPoint implements GeometryPrimitive {

    ImmutableList<Point> points;

}
