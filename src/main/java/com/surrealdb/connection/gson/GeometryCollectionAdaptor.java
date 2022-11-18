package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class GeometryCollectionAdaptor extends SurrealGsonAdaptor<GeometryCollection> {

    GeometryCollectionAdaptor() {
        super(GeometryCollection.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull GeometryCollection src, Type typeOfSrc, @NotNull JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "GeometryCollection");

        JsonArray geometries = new JsonArray();
        object.add("geometries", geometries);

        for (GeometryPrimitive geometry : src) {
            geometries.add(context.serialize(geometry));
        }

        return object;
    }

    @Override
    public @NotNull GeometryCollection deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        JsonArray geometries = object.getAsJsonArray("geometries");

        List<GeometryPrimitive> geometryList = new ArrayList<>(geometries.size());
        for (JsonElement geometry : geometries) {
            // This should be an enhanced switch statement, but that's not supported in Java 8.
            switch (geometry.getAsJsonObject().get("type").getAsString()) {
                case "Point":
                    geometryList.add(context.deserialize(geometry, Point.class));
                    break;
                case "MultiPoint":
                    geometryList.add(context.deserialize(geometry, MultiPoint.class));
                    break;
                case "LineString":
                    geometryList.add(context.deserialize(geometry, LineString.class));
                    break;
                case "MultiLineString":
                    geometryList.add(context.deserialize(geometry, MultiLineString.class));
                    break;
                case "Polygon":
                    geometryList.add(context.deserialize(geometry, Polygon.class));
                    break;
                case "MultiPolygon":
                    geometryList.add(context.deserialize(geometry, MultiPolygon.class));
                    break;
                case "GeometryCollection":
                    geometryList.add(context.deserialize(geometry, GeometryCollection.class));
                    break;
                default:
                    throw new JsonParseException("Unknown geometry type");
            }
        }

        return GeometryCollection.from(geometryList);
    }
}
