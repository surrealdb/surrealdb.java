package com.surrealdb.connection.gson;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.driver.patch.ReplacePatch;
import com.surrealdb.meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Instant;

import static com.surrealdb.meta.utils.GsonTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchReplaceAdaptorTest {

    @Test
    void testIntSerialization() {
        ReplacePatch<Integer> replacePatch = ReplacePatch.create("followers", 32);
        Type type = TypeToken.getParameterized(ReplacePatch.class, Integer.class).getType();
        JsonObject serialized = serialize(replacePatch, type).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "op", "replace");
        assertJsonHasPropertyString(serialized, "path", "followers");
        assertJsonHasPropertyInt(serialized, "value", 32);
    }

    @Test
    void testInstantDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "replace");
        object.addProperty("path", "lastVisit");
        object.addProperty("value", "2020-01-01T00:00:00.000Z");
        Type type = TypeToken.getParameterized(ReplacePatch.class, Instant.class).getType();
        ReplacePatch<Instant> deserialized = GsonTestUtils.deserialize(object, type);

        assertEquals("lastVisit", deserialized.getPath());
        assertEquals(Instant.parse("2020-01-01T00:00:00.000Z"), deserialized.getValue());
    }
}
