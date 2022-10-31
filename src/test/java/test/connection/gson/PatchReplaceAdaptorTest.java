package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.model.patch.ReplacePatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchReplaceAdaptorTest {

    @Test
    void testSerialization() {
        ReplacePatch replacePatch = new ReplacePatch("version", "1.0.1");
        JsonObject serialized = GsonTestUtils.serializeToJsonElement(replacePatch).getAsJsonObject();

        assertEquals("replace", serialized.get("op").getAsString());
        assertEquals("version", serialized.getAsJsonObject().get("path").getAsString());
        assertEquals("1.0.1", serialized.getAsJsonObject().get("value").getAsString());
    }

    @Test
    void testDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "replace");
        object.addProperty("path", "name");
        object.addProperty("value", "Tobie Morgan Hitchcock");
        ReplacePatch deserialized = GsonTestUtils.deserializeFromJsonElement(object, ReplacePatch.class);

        assertEquals("name", deserialized.getPath());
        assertEquals("Tobie Morgan Hitchcock", deserialized.getValue());
    }
}
