package com.surrealdb.driver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author akaecliptic
 */
@Data
@AllArgsConstructor
public class Movie {

	private String id;
	private String title;
	private int rating;
	private LocalDate release;

	public Movie(String title, int rating, LocalDate release) {
		this.title = title;
		this.rating = rating;
		this.release = release;
	}
}
