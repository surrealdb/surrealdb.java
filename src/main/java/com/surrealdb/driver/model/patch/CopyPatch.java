package com.surrealdb.driver.model.patch;

import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
@Getter
public class CopyPatch implements Patch {
    private final String op = "copy";
    private final String from;
    private final String path;

    public CopyPatch(String from, String path) {
        this.from = from;
        this.path = path;
    }

}
