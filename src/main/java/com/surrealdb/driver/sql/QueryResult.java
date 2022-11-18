package com.surrealdb.driver.sql;

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

    @NotNull List<T> result;
    @Nullable String detail;
    @NotNull String status;
    @NotNull String time;

    public @NotNull Optional<String> getDetail() {
        return Optional.ofNullable(detail);
    }
}
