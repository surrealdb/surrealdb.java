package com.surrealdb.query;

import com.google.gson.annotations.SerializedName;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * @author Khalid Alharisi
 */
@Value
public class QueryResult<T> {

    @SerializedName("result")
    @NotNull List<T> result;

    @SerializedName("detail")
    @Nullable String detail;

    @SerializedName("status")
    @NotNull String status;

    @SerializedName("time")
    @NotNull String time;

    public @NotNull Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }
}
