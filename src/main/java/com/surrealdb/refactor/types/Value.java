package com.surrealdb.refactor.types;

import com.google.gson.JsonElement;
import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;

public class Value implements IntoJson {

    @Override
    public JsonElement intoJson() {
        return null;
    }

    public static Value fromJson(JsonElement json) {
        throw SurrealDBUnimplementedException.withTicket("https://github.com/surrealdb/surrealdb.java/issues/63").withMessage("Parsing general values from JSON is not implemented yet.");
    }
}