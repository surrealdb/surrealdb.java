package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.model.patch.AddPatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchAddAdaptorTest {

    @Test
    void testSerialization() {
        AddPatch addPatch = new AddPatch("name", "Damian");
        JsonObject serialized = GsonTestUtils.serializeToJsonElement(addPatch).getAsJsonObject();

        assertEquals("add", serialized.get("op").getAsString());
        assertEquals("name", serialized.getAsJsonObject().get("path").getAsString());
        assertEquals("Damian", serialized.getAsJsonObject().get("value").getAsString());
    }

    @Test
    void testDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "add");
        object.addProperty("path", "name");
        object.addProperty("value", "Tobie Morgan Hitchcock");
        AddPatch deserialized = GsonTestUtils.deserializeFromJsonElement(object, AddPatch.class);

        assertEquals("name", deserialized.getPath());
        assertEquals("Tobie Morgan Hitchcock", deserialized.getValue());
    }
}
