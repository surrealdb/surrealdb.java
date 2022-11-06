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
