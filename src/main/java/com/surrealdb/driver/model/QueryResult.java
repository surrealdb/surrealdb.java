package com.surrealdb.driver.model;

import lombok.Value;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Khalid Alharisi
 */
@Value
public class QueryResult<T> {

    List<T> result;
    @Nullable String detail;
    String status;
    String time;

}
