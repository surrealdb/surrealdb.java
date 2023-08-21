package com.surrealdb.refactor.driver;

import lombok.Getter;

@Getter
public class UseMessage {
    private final String id;
    private final String method = "use";
    private final String[] params;

    public UseMessage(String requestID, String namespace, String database) {
        this.id = requestID;
        // TODO handle these parameters better
        // https://github.com/surrealdb/surrealdb.java/issues/70
        this.params = new String[] {namespace, database};
    }
}
