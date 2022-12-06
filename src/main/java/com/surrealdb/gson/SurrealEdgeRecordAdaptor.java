package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.types.SurrealEdgeRecord;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

import static com.surrealdb.gson.SurrealRecordAdaptor.getId;
import static com.surrealdb.gson.SurrealRecordAdaptor.setId;

final class SurrealEdgeRecordAdaptor extends SurrealGsonAdaptor<SurrealEdgeRecord> {

    @NotNull Gson userGson;

    SurrealEdgeRecordAdaptor(@NonNull Gson userGson) {
        super(SurrealEdgeRecord.class);

        this.userGson = userGson;
    }

    @Override
    public JsonElement serialize(SurrealEdgeRecord edgeRecord, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = userGson.toJsonTree(edgeRecord).getAsJsonObject();

        setId(object, "id", edgeRecord.getId());
        setId(object, "in", edgeRecord.getIn());
        setId(object, "out", edgeRecord.getOut());

        return object;
    }

    @Override
    public SurrealEdgeRecord deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        SurrealEdgeRecord edgeRecord = userGson.fromJson(object, typeOfT);

        edgeRecord.setId(getId(object, "id"));
        edgeRecord.setIn(getId(object, "in"));
        edgeRecord.setOut(getId(object, "out"));

        return edgeRecord;
    }
}
