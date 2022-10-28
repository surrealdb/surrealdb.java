package com.surrealdb.driver.model.signin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DatabaseSignIn {

	private String user;
	private String pass;
	private String NS;
	private String DB;

}
