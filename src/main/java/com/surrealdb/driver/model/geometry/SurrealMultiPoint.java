package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;
import lombok.With;

@Value
@With
public class SurrealMultiPoint implements SurrealGeometryPrimitive {

    ImmutableList<SurrealPoint> points;

}
