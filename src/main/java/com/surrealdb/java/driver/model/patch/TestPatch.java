package com.surrealdb.java.driver.model.patch;

import lombok.Getter;

@Getter
public class TestPatch implements Patch {
    private final String op = "test";
    private final String path;
    private final String value;

    public TestPatch(String path, String value) {
        this.path = path;
        this.value = value;
    }

}
