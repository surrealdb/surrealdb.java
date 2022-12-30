package com.surrealdb.driver.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author flipflopsen
 */
@Data
@RequiredArgsConstructor
public class SignUp {
	@NonNull
	private String ns;
	@NonNull
	private String db;
	@NonNull
	private String sc;
	@NonNull
	private String email;
	@NonNull
	private String pass;
	private boolean marketing;
	private String[] tags;

}
