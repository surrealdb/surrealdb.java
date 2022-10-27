package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;

import java.util.concurrent.ExecutorService;

/**
 * A SurrealDB driver. This driver is used in conjunction with a {@link SurrealConnection} to
 * communicate with the server.
 * <p>
 * This interface isn't meant to be used directly. Take a look at {@link SyncSurrealDriver} or
 * {@link AsyncSurrealDriver} for more information.
 */
// If this library ever gets updated to Java 17, this can be a sealed interface.
public interface SurrealDriver {

    /**
     * @return the {@link SurrealConnection} this driver is using.
     */
    SurrealConnection getSurrealConnection();

    /**
     * @return the {@link ExecutorService} this driver is using for asynchronous operations.
     */
    ExecutorService getAsyncOperationExecutorService();

}
