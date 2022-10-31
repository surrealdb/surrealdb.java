package com.surrealdb.driver.model.patch;

import lombok.*;

/**
 * A patch to change data in an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class ChangePatch<T> implements Patch {

    /**
     * Used by Gson to serialize the patch.
     */
    private final String op = "change";

    private final String path;
    private final T value;

    public static <T> ChangePatch<T> create(String path, T value) {
        return new ChangePatch<>(path, value);
    }

    @Override
    public String getPath() {
        return path;
    }

    public T getValue() {
        return value;
    }
}
