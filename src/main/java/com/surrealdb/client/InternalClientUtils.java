package com.surrealdb.client;

import com.surrealdb.exception.SurrealException;
import com.surrealdb.exception.SurrealExceptionUtils;
import com.surrealdb.query.QueryResult;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Shared methods used by all {@link SurrealClient} implementations. As the class name suggests, these methods are not
 * part of the public API.
 */
@UtilityClass
@ApiStatus.Internal
class InternalClientUtils {

    static <T> @NotNull T getResultSynchronously(@NotNull CompletableFuture<T> future) throws SurrealException {
        try {
            return future.get();
        } catch (Exception exception) {
            throw SurrealExceptionUtils.wrapException(exception.getMessage(), exception);
        }
    }

    static <T> @NotNull List<T> getResultsFromFirstQuery(@NotNull List<QueryResult<T>> queryResults) {
        // If there are no query results, return an empty list
        if (queryResults.isEmpty()) {
            return Collections.emptyList();
        }
        // Since there is at least one QueryResult, it's safe to get the first one
        QueryResult<T> firstQueryResult = queryResults.get(0);
        return firstQueryResult.getResult();
    }

    static <T> @NotNull Optional<T> getFirstResultFromFirstQuery(@NotNull List<QueryResult<T>> queryResults) {
        List<T> resultsFromFirstQuery = getResultsFromFirstQuery(queryResults);
        return getFirstElement(resultsFromFirstQuery);
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
