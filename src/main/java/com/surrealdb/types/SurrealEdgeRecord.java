package com.surrealdb.types;

import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class SurrealEdgeRecord extends SurrealRecord {

    @NonFinal
    transient @Nullable Id in;

    @NonFinal
    transient @Nullable Id out;

    public void setIn(@Nullable Id in) {
        this.in = in;
    }

    public void setOut(@Nullable Id out) {
        this.out = out;
    }

    public @NotNull Optional<@NotNull Id> getIn() {
        return Optional.ofNullable(in);
    }

    public @NotNull Optional<@NotNull Id> getOut() {
        return Optional.ofNullable(out);
    }
}
