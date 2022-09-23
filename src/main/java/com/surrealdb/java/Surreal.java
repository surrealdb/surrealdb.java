package com.surrealdb.java;

import com.surrealdb.java.model.QueryResult;
import com.surrealdb.java.model.patch.Patch;

import java.util.List;
import java.util.Map;

public interface Surreal {

    void signIn(String username, String password);

    void use(String namespace, String database);

    void let(String key, String value);

    <T> List<QueryResult<T>> query(String query, Map<String, String> args, Class<? extends T> rowClass);

    <T> List<T> select(String thing, Class<? extends T> rowType);

    <T> T create(String thing, T data);

    <T> List<T> update(String thing, T data);

    <T, P> List<T> change(String thing, P data, Class<T> rowType);

    void patch(String thing, List<Patch> patches);

    void delete(String thing);

}
