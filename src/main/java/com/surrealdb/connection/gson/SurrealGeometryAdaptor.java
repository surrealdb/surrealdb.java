package com.surrealdb.connection.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.driver.model.geometry.SurrealGeometryPrimitive;
import com.surrealdb.driver.model.geometry.SurrealPoint;

public abstract class SurrealGeometryAdaptor<T extends SurrealGeometryPrimitive> implements SurrealGsonAdaptor<T> {

    private final Class<T> adaptorClass;

    protected SurrealGeometryAdaptor(Class<T> adaptorClass) {
        this.adaptorClass = adaptorClass;
    }

    protected JsonObject createJsonObject(String type, JsonArray coordinates) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.add("coordinates", coordinates);
        return object;
    }

    protected JsonArray getCoordinates(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        return object.getAsJsonArray("coordinates");
    }

    protected JsonArray serializePointToArray(SurrealPoint point) {
        return serializePointToArray(point.getLongitude(), point.getLatitude());
    }

    protected JsonArray serializePointToArray(double longitude, double latitude) {
        JsonArray pointArray = new JsonArray();
        pointArray.add(longitude);
        pointArray.add(latitude);
        return pointArray;
    }

    protected SurrealPoint deserializePointFromArray(JsonArray coordinates) {
        double longitude = coordinates.get(0).getAsDouble();
        double latitude = coordinates.get(1).getAsDouble();
        return SurrealPoint.fromLongitudeLatitude(longitude, latitude);
    }

    @Override
    public Class<T> getAdaptorClass() {
        return adaptorClass;
    }
}
