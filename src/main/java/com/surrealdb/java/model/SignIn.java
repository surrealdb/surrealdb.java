package com.surrealdb.java.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignIn {
    private String user;
    private String pass;
}
