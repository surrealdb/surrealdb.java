package com.surrealdb.driver.patch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A patch to modify data in an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class ReplacePatch<T> implements Patch {

    @NotNull String op = "replace";

    @NotNull String path;
    @NotNull T value;

    public static <T> @NotNull ReplacePatch<T> create(@NotNull String path, @NotNull T value) {
        return new ReplacePatch<>(path, value);
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }

    public @NotNull T getValue() {
        return value;
    }
}
