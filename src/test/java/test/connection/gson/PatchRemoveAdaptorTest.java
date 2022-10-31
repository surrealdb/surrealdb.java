package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.model.patch.RemovePatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.assertJsonHasPropertyString;

public class PatchRemoveAdaptorTest {

    @Test
    void testSerialization() {
        RemovePatch removePatch = new RemovePatch("valueToRemove");
        JsonObject serialized = GsonTestUtils.serializeToJsonElement(removePatch).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "op", "remove");
        assertJsonHasPropertyString(serialized, "path", "valueToRemove");
    }

    @Test
    void testDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "remove");
        object.addProperty("path", "valueToRemove");
        RemovePatch deserialized = GsonTestUtils.deserializeFromJsonElement(object, RemovePatch.class);

        assertEquals("valueToRemove", deserialized.getPath());
    }
}
