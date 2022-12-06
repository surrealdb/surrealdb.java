package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.types.Id;
import com.surrealdb.types.SurrealRecord;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;

final class SurrealRecordAdaptor extends SurrealGsonAdaptor<SurrealRecord> {

    private Gson userGson;

    SurrealRecordAdaptor(Gson userGson) {
        super(SurrealRecord.class);
        this.userGson = userGson;
    }

    @Override
    public JsonElement serialize(SurrealRecord record, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = userGson.toJsonTree(record).getAsJsonObject();

        setId(object, "id", record.getId());

        return object;
    }

    @Override
    public SurrealRecord deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        SurrealRecord record = userGson.fromJson(object, typeOfT);

        record.setId(getId(object, "id"));

        return record;
    }

    static @Nullable Id getId(@NotNull JsonObject object, @NotNull String idKey) {
        JsonPrimitive idPrimitive = object.getAsJsonPrimitive(idKey);

        if (idPrimitive == null) {
            return null;
        }

        String idString = idPrimitive.getAsString();
        return Id.parse(idString);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static void setId(@NotNull JsonObject object, @NotNull String idKey, Optional<Id> id) {
        id.ifPresent(presentId -> object.addProperty(idKey, presentId.toCombinedId()));
    }
}
