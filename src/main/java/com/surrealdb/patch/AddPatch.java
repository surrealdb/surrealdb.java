package com.surrealdb.patch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A patch to add data to an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class AddPatch<T> implements Patch {

    /**
     * Used by Gson to serialize the patch.
     */
    @NotNull String op = "add";

    @NotNull String path;
    @NotNull T value;

    public static <T> @NotNull AddPatch<T> create(@NotNull String path, @NotNull T value) {
        return new AddPatch<>(path, value);
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }

    public @NotNull T getValue() {
        return value;
    }
}