package com.surrealdb.driver.model.patch;

/**
 * @author Khalid Alharisi
 */
public record ReplacePatch(String path, String value) implements Patch {
    private static final String op = "replace";
}
