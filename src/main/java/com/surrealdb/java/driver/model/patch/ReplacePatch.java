package com.surrealdb.java.driver.model.patch;

import lombok.Getter;

@Getter
public class ReplacePatch implements Patch {
    private final String op = "replace";
    private final String path;
    private final String value;

    public ReplacePatch(String path, String value) {
        this.path = path;
        this.value = value;
    }

}
