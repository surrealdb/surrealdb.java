package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;

@Value
public class SurrealMultiPolygon implements SurrealGeometryPrimitive {

    ImmutableList<SurrealPolygon> polygons;

}
