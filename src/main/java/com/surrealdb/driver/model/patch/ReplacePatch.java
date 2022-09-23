package com.surrealdb.driver.model.patch;

import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
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
