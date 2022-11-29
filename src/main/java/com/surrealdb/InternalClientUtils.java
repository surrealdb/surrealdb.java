package com.surrealdb;

import com.surrealdb.connection.exception.SurrealException;
import com.surrealdb.driver.sql.QueryResult;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Shared methods used by all {@link SurrealClient} implementations. As the class name suggests, these methods are not
 * part of the public API.
 */
@UtilityClass
class InternalClientUtils {

    static <T> T getResultSynchronously(@NotNull CompletableFuture<T> completableFuture) {
        try {
            return completableFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof SurrealException) {
                throw (SurrealException) e.getCause();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    static <T> @NotNull Optional<T> getFirstResultFromFirstQuery(@NotNull List<QueryResult<T>> queryResults) {
        List<T> resultsFromFirstQuery = getResultsFromFirstQuery(queryResults);
        return getFirstElement(resultsFromFirstQuery);
    }

    static <T> @NotNull List<T> getResultsFromFirstQuery(@NotNull List<QueryResult<T>> queryResults) {
        // If there are no query results, return an empty list
        if (queryResults.isEmpty()) {
            return Collections.emptyList();
        }
        // Since there is at least one query, it's safe to get the first one
        QueryResult<T> firstQueryResult = queryResults.get(0);
        return firstQueryResult.getResult();
    }

    /**
     * @param list The list to get the first element from
     * @param <T>  The type of the list
     * @return the first element in the list or an empty {@link Optional} if the list is empty
     */
    static <T> @NotNull Optional<T> getFirstElement(@NotNull List<T> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
