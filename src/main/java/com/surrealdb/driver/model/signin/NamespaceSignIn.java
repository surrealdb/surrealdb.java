package com.surrealdb.driver.model.signin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NamespaceSignIn {

	private String user;
	private String pass;
	private String NS;

}
