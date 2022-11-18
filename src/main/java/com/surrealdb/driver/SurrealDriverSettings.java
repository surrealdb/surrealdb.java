package com.surrealdb.driver;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * An immutable configuration object that can be used to configure the behavior of a {@code SurrealDriver}. To create a
 * new {@code SurrealDriverSettings} object, call {@link SurrealDriverSettings.Builder}.
 */
@Builder(builderClassName = "Builder", setterPrefix = "set")
@Getter
@With
public final class SurrealDriverSettings {

    public static final SurrealDriverSettings DEFAULT = SurrealDriverSettings.builder().build();

    /**
     * The {@link ExecutorService} to use for asynchronous operations. If not provided, a
     * {@link ForkJoinPool} will be used.
     */
    @lombok.Builder.Default
    @NotNull ExecutorService asyncOperationExecutorService = ForkJoinPool.commonPool();

}
