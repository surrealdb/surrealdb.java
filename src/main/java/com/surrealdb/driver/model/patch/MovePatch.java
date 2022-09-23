package com.surrealdb.driver.model.patch;

import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
@Getter
public class MovePatch implements Patch {
    private final String op = "move";
    private final String from;
    private final String path;

    public MovePatch(String from, String path) {
        this.from = from;
        this.path = path;
    }

}
