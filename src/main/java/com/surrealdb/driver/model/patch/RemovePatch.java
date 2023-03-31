package com.surrealdb.driver.model.patch;

/**
 * @author Khalid Alharisi
 */
public record RemovePatch(String op, String path) implements Patch {
    public RemovePatch(String path) {
        this("remove", path);
    }
}
