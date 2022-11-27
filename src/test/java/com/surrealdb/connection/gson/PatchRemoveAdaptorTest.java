package com.surrealdb.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.patch.RemovePatch;
import com.surrealdb.meta.GsonAdaptorTest;
import com.surrealdb.meta.utils.GsonTestUtils;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GsonTestUtils.assertJsonHasPropertyString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchRemoveAdaptorTest extends GsonAdaptorTest {

    @Test
    @Override
    protected void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation() {
        RemovePatch removePatch = RemovePatch.create("valueToRemove");
        JsonObject serialized = GsonTestUtils.serialize(removePatch).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "op", "remove");
        assertJsonHasPropertyString(serialized, "path", "valueToRemove");
    }

    @Test
    @Override
    protected void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "remove");
        object.addProperty("path", "valueToRemove");
        RemovePatch deserialized = GsonTestUtils.deserialize(object, RemovePatch.class);

        assertEquals("valueToRemove", deserialized.getPath());
    }
}
