package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;

import java.util.concurrent.ExecutorService;

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
