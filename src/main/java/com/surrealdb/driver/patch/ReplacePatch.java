package com.surrealdb.driver.patch;

import lombok.*;

/**
 * A patch to modify data in an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class ReplacePatch<T> implements Patch {

    private final String op = "replace";

    private final String path;
    private final T value;

    public static <T> ReplacePatch<T> create(String path, T value) {
        return new ReplacePatch<>(path, value);
    }

    @Override
    public String getPath() {
        return path;
    }

    public T getValue() {
        return value;
    }
}
