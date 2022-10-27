package com.surrealdb.driver;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

@Builder(builderClassName = "Builder", setterPrefix = "set")
@Getter
@With
public class SurrealDriverSettings {

    public static final SurrealDriverSettings DEFAULT = SurrealDriverSettings.builder().build();

    /**
     * The {@link ExecutorService} to use for asynchronous operations. If not provided, a
     * {@link ForkJoinPool} will be used.
     */
    @lombok.Builder.Default
    private final ExecutorService asyncOperationExecutorService = ForkJoinPool.commonPool();

}
