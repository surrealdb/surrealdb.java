package com.surrealdb.driver;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SurrealTable<T> {

    String name;
    Class<T> type;

    public static <T> SurrealTable<T> create(String name, Class<T> type) {
        return new SurrealTable<>(name, type);
    }

    public <U> SurrealTable<U> withType(Class<U> type) {
        return new SurrealTable<>(name, type);
    }

    String makeThing(String record) {
        return name + ":" + record;
    }
}
