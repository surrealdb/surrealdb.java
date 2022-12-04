package com.surrealdb.types;

import com.google.gson.annotations.SerializedName;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SurrealRecord {

    @SerializedName("id")
    @NonFinal
    @Nullable Id id;

    public void setId(@Nullable Id id) {
        this.id = id;
    }

    public @NotNull Optional<@NotNull Id> getId() {
        return Optional.ofNullable(id);
    }
}
