package com.surrealdb.types;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@ToString
public abstract class SurrealEdgeRecord extends SurrealRecord {

    @NonFinal
    @SerializedName("in")
    transient @Nullable Id in;

    @NonFinal
    @SerializedName("out")
    @Nullable Id out;

    public SurrealEdgeRecord(@Nullable Id id, @Nullable Id in, @Nullable Id out) {
        super(id);

        this.in = in;
        this.out = out;
    }

    public SurrealEdgeRecord() {
        this.in = null;
        this.out = null;
    }

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
