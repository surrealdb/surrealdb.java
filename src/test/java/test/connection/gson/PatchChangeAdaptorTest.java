package test.connection.gson;

import com.google.gson.JsonObject;
import com.surrealdb.driver.model.patch.AddPatch;
import com.surrealdb.driver.model.patch.ChangePatch;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchChangeAdaptorTest {

    @Test
    void testSerialization() {
        ChangePatch changePatch = new ChangePatch("version", "1.0.0");
        JsonObject serialized = GsonTestUtils.serializeToJsonElement(changePatch).getAsJsonObject();

        assertEquals("change", serialized.get("op").getAsString());
        assertEquals("version", serialized.getAsJsonObject().get("path").getAsString());
        assertEquals("1.0.0", serialized.getAsJsonObject().get("value").getAsString());
    }

    @Test
    void testDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "change");
        object.addProperty("path", "name");
        object.addProperty("value", "Tobie Morgan Hitchcock");
        AddPatch deserialized = GsonTestUtils.deserializeFromJsonElement(object, AddPatch.class);

        assertEquals("name", deserialized.getPath());
        assertEquals("Tobie Morgan Hitchcock", deserialized.getValue());
    }
}
