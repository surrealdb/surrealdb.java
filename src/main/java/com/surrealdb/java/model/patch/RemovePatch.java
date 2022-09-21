package com.surrealdb.java.model.patch;

import lombok.Getter;

@Getter
public class RemovePatch implements Patch {
    private final String op = "remove";
    private final String path;

    public RemovePatch(String path) {
        this.path = path;
    }

}
