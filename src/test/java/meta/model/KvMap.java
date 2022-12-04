package meta.model;

import com.surrealdb.types.SurrealRecord;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class KvMap<K, V> extends SurrealRecord {

    private final @NotNull Map<K, V> map = new LinkedHashMap<>();

    public void put(@NotNull K key, @NotNull V value) {
        map.put(key, value);
    }

    public @NotNull V get(@NotNull K key) {
        return map.get(key);
    }

    public static void assertKvMapEquals(@NotNull KvMap<?, ?> expected, @NotNull KvMap<?, ?> actual) {
        assertEquals(expected.map.size(), actual.map.size(), "Maps have different sizes");

        expected.map.forEach((key, value) -> {
            assertTrue(actual.map.containsKey(key), "Actual map does not contain key " + key);
            assertEquals(value, actual.map.get(key), "Values are not equal for key " + key);
        });
    }
}
