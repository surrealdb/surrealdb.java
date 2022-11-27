package com.surrealdb.connection.gson;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.driver.patch.ChangePatch;
import com.surrealdb.meta.GsonAdaptorTest;
import com.surrealdb.meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Instant;

import static com.surrealdb.meta.utils.GsonTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchChangeAdaptorTest extends GsonAdaptorTest {

    @Test
    @Override
    protected void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation() {
        ChangePatch<Integer> changePatch = ChangePatch.create("followers", 32);
        Type type = TypeToken.getParameterized(ChangePatch.class, Integer.class).getType();
        JsonObject serialized = serialize(changePatch, type).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "op", "change");
        assertJsonHasPropertyString(serialized, "path", "followers");
        assertJsonHasPropertyInt(serialized, "value", 32);
    }

    @Test
    @Override
    protected void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "change");
        object.addProperty("path", "lastVisit");
        object.addProperty("value", "2020-01-01T00:00:00.000Z");
        Type type = TypeToken.getParameterized(ChangePatch.class, Instant.class).getType();
        ChangePatch<Instant> deserialized = GsonTestUtils.deserialize(object, type);

        assertEquals("lastVisit", deserialized.getPath());
        assertEquals(Instant.parse("2020-01-01T00:00:00.000Z"), deserialized.getValue());
    }
}
