package com.surrealdb.driver.model;

import java.util.List;

/**
 * @author Khalid Alharisi
 */
public record QueryResult<T>(List<T> result, String status, String time) {}
