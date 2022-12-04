package com.surrealdb.patch;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("op")
    @NotNull String operation = "change";

    @SerializedName("path")
    @NotNull String path;

    @SerializedName("value")
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
