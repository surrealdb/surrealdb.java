package test.driver.model;

import com.surrealdb.driver.geometry.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Data
@Accessors(chain = true)
public class GeoContainer {

    @NotNull
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
