package com.surrealdb.java.model.patch;

import lombok.Getter;

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
