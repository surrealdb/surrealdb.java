package com.surrealdb.refactor;

public class Tuple<K, V> {
    final K key;
    final V value;

    private Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Tuple<K, V> of(K key, V value) {
        return new Tuple<>(key, value);
    }
}
