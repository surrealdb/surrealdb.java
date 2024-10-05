package com.surrealdb;

public enum UpType {
    CONTENT(1),
    MERGE(2),
    PATCH(3);

    final int code;

    UpType(int code) {
        this.code = code;
    }
    
}
