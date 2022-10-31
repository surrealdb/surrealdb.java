package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;

/**
 * MultiLines can be used to store multiple geometry lines in a single value.
 *
 * @author Damian Kocher
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#multiline">SurrealDB Docs - MultiLine</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.5">GeoJSON - MultiLine</a>
 */
@Value
public class MultiLine implements GeometryPrimitive {

    ImmutableList<Line> lines;

}
