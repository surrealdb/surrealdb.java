package com.surrealdb.driver.patch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A patch to change data in an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class ChangePatch<T> implements Patch {

    /**
     * Used by Gson to serialize the patch.
     */
    String op = "change";

    @NotNull String path;
    @NotNull T value;

    public static <T> @NotNull ChangePatch<T> create(@NotNull String path, @NotNull T value) {
        return new ChangePatch<>(path, value);
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }

    public @NotNull T getValue() {
        return value;
    }
}
