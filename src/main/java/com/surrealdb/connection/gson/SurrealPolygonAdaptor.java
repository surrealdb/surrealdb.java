package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealPoint;
import com.surrealdb.driver.model.geometry.SurrealPolygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SurrealPolygonAdaptor extends SurrealGeometryAdaptor<SurrealPolygon> {

    protected SurrealPolygonAdaptor() {
        super(SurrealPolygon.class);
    }

    @Override
    public JsonElement serialize(SurrealPolygon src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        coordinates.add(serializeLinearRing(src.getOuterRing()));

        Optional<ImmutableList<SurrealPoint>> innerRing = src.getInnerRing();
        innerRing.ifPresent(surrealPoints -> coordinates.add(serializeLinearRing(surrealPoints)));

        return createJsonObject("Polygon", coordinates);
    }

    private JsonArray serializeLinearRing(ImmutableList<SurrealPoint> linearRing) {
        JsonArray array = new JsonArray();
        for (SurrealPoint point : linearRing) {
            array.add(serializePointToArray(point));
        }
        // Add the first point again to close the ring
        array.add(array.get(0));

        return array;
    }

    @Override
    public SurrealPolygon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        ImmutableList<SurrealPoint> outerRing = deserializeLinearRing(coordinates.get(0).getAsJsonArray());
        ImmutableList<SurrealPoint> innerRing = null;
        if (coordinates.size() > 1) {
            innerRing = deserializeLinearRing(coordinates.get(1).getAsJsonArray());
        }
        return new SurrealPolygon(outerRing, innerRing);
    }

    private ImmutableList<SurrealPoint> deserializeLinearRing(JsonElement element) {
        JsonArray pointCoordinates = element.getAsJsonArray();
        List<SurrealPoint> points = new ArrayList<>(pointCoordinates.size());

        // Ignore the last point, as it is the same as the first
        for (int index = 0; index < pointCoordinates.size() - 1; index++) {
            points.add(deserializePointFromArray(pointCoordinates.get(index).getAsJsonArray()));
        }

        return ImmutableList.copyOf(points);
    }

    @Override
    public Class<SurrealPolygon> getAdaptorClass() {
        return SurrealPolygon.class;
    }
}
