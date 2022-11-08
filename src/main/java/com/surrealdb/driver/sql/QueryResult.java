package com.surrealdb.driver.sql;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Khalid Alharisi
 */
@Value
public class QueryResult<T> {

    @NotNull List<T> result;
    @Nullable String detail;
    @NotNull String status;
    @NotNull String time;

}
