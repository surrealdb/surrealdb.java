package test.connection.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.surrealdb.connection.gson.SurrealGsonUtils;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GsonTestUtils {

    private static final Gson gsonInstance = SurrealGsonUtils.makeGsonInstanceSurrealCompatible(new Gson());

    public static <T> JsonElement serializeToJsonElement(T object) {
        return gsonInstance.toJsonTree(object);
    }

    public static <T> T deserializeFromJsonElement(JsonElement jsonElement, Class<T> clazz) {
        return gsonInstance.fromJson(jsonElement, clazz);
    }
}
