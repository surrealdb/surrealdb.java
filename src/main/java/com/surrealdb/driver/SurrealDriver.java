package com.surrealdb.driver;

import java.util.concurrent.ExecutorService;

// If this library ever gets updated to Java 17, this can be a sealed interface.
public interface SurrealDriver {

    ExecutorService getAsyncOperationExecutorService();

}
