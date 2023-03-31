package com.surrealdb.driver.model.patch;

/**
 * @author Khalid Alharisi
 */
public record ReplacePatch(String op, String path, String value) implements Patch {
    public ReplacePatch(String path, String value) {
        this("replace", path, value);
    }
}
