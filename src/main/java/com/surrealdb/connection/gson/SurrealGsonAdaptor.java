package com.surrealdb.connection.gson;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface SurrealGsonAdaptor<T> extends JsonSerializer<T>, JsonDeserializer<T> {

}
