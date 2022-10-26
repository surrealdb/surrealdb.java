package com.surrealdb.driver.model;

import lombok.Value;

import java.util.List;

/**
 * @author Khalid Alharisi
 */
@Value
public class QueryResult<T> {

    List<T> result;
    String status;
    String time;

}
