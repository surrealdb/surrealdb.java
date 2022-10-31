package test.connection.gson;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.driver.model.patch.AddPatch;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.*;

public class PatchAddAdaptorTest {

    @Test
    void testIntSerialization() {
        AddPatch<Integer> addPatch = AddPatch.create("followers", 32);
        Type type = TypeToken.getParameterized(AddPatch.class, Integer.class).getType();
        JsonObject serialized = serializeToJsonElement(addPatch, type).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "op", "add");
        assertJsonHasPropertyString(serialized, "path", "followers");
        assertJsonHasPropertyInt(serialized, "value", 32);
    }

    @Test
    void testInstantDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "add");
        object.addProperty("path", "lastVisit");
        object.addProperty("value", "2020-01-01T00:00:00.000Z");
        Type type = TypeToken.getParameterized(AddPatch.class, Instant.class).getType();
        AddPatch<Instant> deserialized = GsonTestUtils.deserializeFromJsonElement(object, type);

        assertEquals("lastVisit", deserialized.getPath());
        assertEquals(Instant.parse("2020-01-01T00:00:00.000Z"), deserialized.getValue());
    }
}
