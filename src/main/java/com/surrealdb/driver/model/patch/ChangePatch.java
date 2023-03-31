package com.surrealdb.driver.model.patch;

/**
 * @author Khalid Alharisi
 */
public record ChangePatch(String op, String path, String value) implements Patch {
    public ChangePatch(String path, String value) {
        this("change", path, value);
    }
}
