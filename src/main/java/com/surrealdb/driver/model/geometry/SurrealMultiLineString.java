package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;

@Value
public class SurrealMultiLineString implements SurrealGeometryPrimitive {

    ImmutableList<SurrealLineString> lines;
}
