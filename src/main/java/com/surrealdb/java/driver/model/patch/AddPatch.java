package com.surrealdb.java.driver.model.patch;

import lombok.Getter;

@Getter
public class AddPatch implements Patch {
    private final String op = "add";
    private final String path;
    private final String value;

    public AddPatch(String path, String value) {
        this.path = path;
        this.value = value;
    }

}
