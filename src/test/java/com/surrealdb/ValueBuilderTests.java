package com.surrealdb;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.Object;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ValueBuilder tests
 *
 * @see ValueBuilder
 * @author honhimW
 */

public class ValueBuilderTests {

    @BeforeAll
    static void loadNative() {
        Loader.loadNative();
    }

    @Test
    void convertMap2ValueMut() {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", "1");
        record.put("name", "John Doe");
        record.put("tags", Arrays.asList("tag1", "tag2"));
        record.put("active", true);
        record.put("age", 18L);
        record.put("emails", Arrays.asList("email1", "email2"));
        Assertions.assertDoesNotThrow(() -> ValueBuilder.convert(record));
    }

}
