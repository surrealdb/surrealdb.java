package com.surrealdb.driver.model.signin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Khalid Alharisi
 */
@Data
@AllArgsConstructor
public class SignIn {
    private String user;
    private String pass;
}
