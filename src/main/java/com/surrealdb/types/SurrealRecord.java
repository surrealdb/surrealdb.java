package com.surrealdb.types;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EqualsAndHashCode
@ToString
public abstract class SurrealRecord {

    @NonFinal
    @SerializedName("id")
    @Nullable Id id;

    public SurrealRecord(@Nullable Id id) {
        this.id = id;
    }

    public SurrealRecord() {
        id = null;
    }

    public void setId(@Nullable Id id) {
        this.id = id;
    }

    public @NotNull Optional<@NotNull Id> getId() {
        return Optional.ofNullable(id);
    }
}
