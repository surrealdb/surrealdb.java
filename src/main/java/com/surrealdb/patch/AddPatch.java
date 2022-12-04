package com.surrealdb.patch;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("op")
    @NotNull String operation = "add";

    @SerializedName("path")
    @NotNull String path;

    @SerializedName("value")
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
