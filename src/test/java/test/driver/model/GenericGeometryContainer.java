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
    Point point;
    @Nullable
    Line line;
    @Nullable
    Polygon polygon;
    @Nullable
    MultiPoint multiPoint;
    @Nullable
    MultiLine multiLine;
    @Nullable
    MultiPolygon multiPolygon;
    @Nullable
    GeometryCollection geometryCollection;

}
