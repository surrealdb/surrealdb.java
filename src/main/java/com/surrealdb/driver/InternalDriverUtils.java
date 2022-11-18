package com.surrealdb.driver;

import com.surrealdb.connection.exception.SurrealException;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Shared methods used by all {@link SurrealDriver} implementations. As the class name suggests, these methods are not
 * part of the public API.
 */
@UtilityClass
class InternalDriverUtils {

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
}
