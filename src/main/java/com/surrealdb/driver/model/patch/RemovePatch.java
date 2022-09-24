package com.surrealdb.driver.model.patch;

import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
@Getter
public class RemovePatch implements Patch {
    private final String op = "remove";
    private final String path;

    public RemovePatch(String path) {
        this.path = path;
    }

}
