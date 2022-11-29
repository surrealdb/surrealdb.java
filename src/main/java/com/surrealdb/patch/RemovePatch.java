package com.surrealdb.patch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A patch to remove data from an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class RemovePatch implements Patch {

    /**
     * Used by Gson to serialize the patch.
     */
    @NotNull String op = "remove";

    @NotNull String path;

    public static @NotNull RemovePatch create(@NotNull String path) {
        return new RemovePatch(path);
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }
}
