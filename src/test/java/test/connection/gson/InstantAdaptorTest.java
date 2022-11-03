package test.connection.gson;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstantAdaptorTest {

    @Test
    void testStartOfEpoch() {
        Instant startOfEpoch = Instant.ofEpochMilli(0);
        String expectedSerialized = "1970-01-01T00:00:00Z";

        testInstantSerialization(startOfEpoch, expectedSerialized);
    }

    @Test
    void testNineDigitsOfNanosecondPrecision() {
        Instant instant = Instant.ofEpochSecond(0, 123_456_789);
        String expectedSerialized = "1970-01-01T00:00:00.123456789Z";

        testInstantSerialization(instant, expectedSerialized);
    }

    @Test
    void testSixDigitsOfNanosecondPrecision() {
        Instant instant = Instant.ofEpochSecond(0, 123_456_000);
        String expectedSerialized = "1970-01-01T00:00:00.123456Z";

        testInstantSerialization(instant, expectedSerialized);
    }

    @Test
    void testThreeDigitsOfNanosecondPrecision() {
        Instant instant = Instant.ofEpochSecond(0, 555_000_000);
        String expectedSerialized = "1970-01-01T00:00:00.555Z";

        testInstantSerialization(instant, expectedSerialized);
    }

    private void testInstantSerialization(Instant instant, String expectedSerialized) {
        // Serialize to JSON Element and check the serialized value matches the expected value
        JsonElement serialized = GsonTestUtils.serialize(instant);
        String actualSerialized = serialized.getAsString();
        assertEquals(expectedSerialized, actualSerialized, "Check serialized value matches expected");

        // Deserialize from JSON Element and check the deserialized instant matches the original instant
        Instant actualDeserialized = GsonTestUtils.deserialize(serialized, Instant.class);
        assertEquals(instant, actualDeserialized, "Check deserialized value matches expected");
    }
}
