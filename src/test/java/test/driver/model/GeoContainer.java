package test.driver.model;

import com.surrealdb.driver.geometry.*;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

@Data
@Accessors(chain = true)
public class GeoContainer {

    @NonNull
    String name;
    @Nullable
    Point point;
    @Nullable
    LineString line;
    @Nullable
    Polygon polygon;
    @Nullable
    MultiPoint multiPoint;
    @Nullable
    MultiLineString multiLineString;
    @Nullable
    MultiPolygon multiPolygon;
    @Nullable
    GeometryCollection geometryCollection;

}
