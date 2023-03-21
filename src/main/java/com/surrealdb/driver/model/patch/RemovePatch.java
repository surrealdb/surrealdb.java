package com.surrealdb.driver.model.patch;

/**
 * @author Khalid Alharisi
 */
public record RemovePatch(String path) implements Patch {
    private static final String op = "remove";
}
