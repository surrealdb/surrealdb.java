package com.surrealdb.types;

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
    transient @Nullable Id id;

    public void setId(@Nullable Id id) {
        this.id = id;
    }

    public @NotNull Optional<@NotNull Id> getId() {
        return Optional.ofNullable(id);
    }
}
