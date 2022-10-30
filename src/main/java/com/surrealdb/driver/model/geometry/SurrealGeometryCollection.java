package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;

@Value
public class SurrealGeometryCollection {

    ImmutableList<SurrealGeometryPrimitive> geometries;

}
