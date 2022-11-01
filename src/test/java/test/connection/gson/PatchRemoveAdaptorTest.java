package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.model.patch.RemovePatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.assertJsonHasPropertyString;

public class PatchRemoveAdaptorTest {

    @Test
    void testSerialization() {
        RemovePatch removePatch = RemovePatch.create("valueToRemove");
        JsonObject serialized = GsonTestUtils.serialize(removePatch).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "op", "remove");
        assertJsonHasPropertyString(serialized, "path", "valueToRemove");
    }

    @Test
    void testDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "remove");
        object.addProperty("path", "valueToRemove");
        RemovePatch deserialized = GsonTestUtils.deserialize(object, RemovePatch.class);

        assertEquals("valueToRemove", deserialized.getPath());
    }
}
