package com.surrealdb.query;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryCollection<T> implements Iterable<QueryResult<T>> {

    private @NotNull ImmutableList<QueryResult<T>> queries;

    public static <T> @NotNull QueryCollection<T> from(@NonNull List<QueryResult<T>> queries) {
        return new QueryCollection<>(ImmutableList.copyOf(queries));
    }

    /**
     * @param index The index of the query to get
     * @return the query at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= queryCount())
     */
    public @NotNull QueryResult<T> getQueryResult(int index) {
        return queries.get(index);
    }

    public int queryCount() {
        return queries.size();
    }

    @NonNull
    @Override
    public Iterator<QueryResult<T>> iterator() {
        return queries.iterator();
    }
}
