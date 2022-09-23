package com.surrealdb.java.driver.model;

import lombok.Data;

import java.util.List;

@Data
public class QueryResult<T> {
    private List<T> result;
    private String status;
    private String time;
}
