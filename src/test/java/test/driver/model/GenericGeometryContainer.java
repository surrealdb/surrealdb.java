package test.driver.model;

import com.surrealdb.driver.model.geometry.*;
import lombok.Data;
import lombok.NonNull;

import javax.annotation.Nullable;

@Data
public class GenericGeometryContainer {

    @NonNull
    String name;
    @Nullable
    SurrealPoint point;
    @Nullable
    SurrealLineString lineString;
    @Nullable
    SurrealPolygon polygon;
    @Nullable
    SurrealMultiPoint multiPoint;
    @Nullable
    SurrealMultiLineString multiLineString;
    @Nullable
    SurrealMultiPolygon multiPolygon;
    @Nullable
    SurrealGeometryCollection geometryCollection;

}
