package com.surrealdb.driver.patch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A patch to add data to an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class AddPatch<T> implements Patch {

    /**
     * Used by Gson to serialize the patch.
     */
    private final String op = "add";

    private final String path;
    private final T value;

    public static <T> AddPatch<T> create(String path, T value) {
        return new AddPatch<>(path, value);
    }

    @Override
    public String getPath() {
        return path;
    }

    public T getValue() {
        return value;
    }
}
