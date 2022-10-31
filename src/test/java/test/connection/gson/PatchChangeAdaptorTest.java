package test.connection.gson;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.driver.model.patch.ChangePatch;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.connection.gson.GsonTestUtils.*;

public class PatchChangeAdaptorTest {

    @Test
    void testIntSerialization() {
        ChangePatch<Integer> changePatch = ChangePatch.create("followers", 32);
        Type type = TypeToken.getParameterized(ChangePatch.class, Integer.class).getType();
        JsonObject serialized = serializeToJsonElement(changePatch, type).getAsJsonObject();

        assertJsonHasPropertyString(serialized, "op", "change");
        assertJsonHasPropertyString(serialized, "path", "followers");
        assertJsonHasPropertyInt(serialized, "value", 32);
    }

    @Test
    void testInstantDeserialization() {
        JsonObject object = new JsonObject();
        object.addProperty("op", "change");
        object.addProperty("path", "lastVisit");
        object.addProperty("value", "2020-01-01T00:00:00.000Z");
        Type type = TypeToken.getParameterized(ChangePatch.class, Instant.class).getType();
        ChangePatch<Instant> deserialized = GsonTestUtils.deserializeFromJsonElement(object, type);

        assertEquals("lastVisit", deserialized.getPath());
        assertEquals(Instant.parse("2020-01-01T00:00:00.000Z"), deserialized.getValue());
    }
}
