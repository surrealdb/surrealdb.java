package com.surrealdb.driver.model.patch;

/**
 * @author Khalid Alharisi
 */
public record AddPatch(String op, String path, String value) implements Patch {
    public AddPatch(String path, String value) {
        this("add", path, value);
    }
}
