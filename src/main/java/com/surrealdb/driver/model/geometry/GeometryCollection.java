package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;

@Value
public class GeometryCollection {

    ImmutableList<GeometryPrimitive> geometries;

}
