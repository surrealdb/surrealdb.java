package com.surrealdb.driver.model;

import lombok.Data;

import java.util.List;

/**
 * @author Khalid Alharisi
 */
@Data
public class QueryResult<T> {
	private List<T> result;
	private String status;
	private String time;
}
