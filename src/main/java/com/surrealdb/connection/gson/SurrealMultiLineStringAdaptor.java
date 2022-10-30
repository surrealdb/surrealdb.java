package com.surrealdb.connection.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.surrealdb.driver.model.geometry.SurrealMultiLineString;

import java.lang.reflect.Type;

public class SurrealMultiLineStringAdaptor extends SurrealGeometryAdaptor<SurrealMultiLineString> {

    protected SurrealMultiLineStringAdaptor() {
        super(SurrealMultiLineString.class);
    }

    @Override
    public JsonElement serialize(SurrealMultiLineString src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

    @Override
    public SurrealMultiLineString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
