package com.surrealdb.types;

import com.google.gson.annotations.SerializedName;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SurrealEdgeRecord extends SurrealRecord {

    @SerializedName("from")
    @NonFinal
    @Nullable Id in;

    @SerializedName("out")
    @NonFinal
    @Nullable Id out;

    public void setInAndOut(@NotNull Id in, @NotNull Id out) {
        this.in = in;
        this.out = out;
    }

    public @NotNull Optional<@NotNull Id> getIn() {
        return Optional.ofNullable(in);
    }

    public @NotNull Optional<@NotNull Id> getOut() {
        return Optional.ofNullable(out);
    }
}
