package com.surrealdb.meta;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SurrealDriver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public abstract class SurrealDriver_MockTest {

    protected abstract SurrealDriver createDriver(SurrealConnection connection);

    @Test
    void ping_whenCalled_callsRpcOnConnection() {
        SurrealConnection mockConnection = mock(SurrealWebSocketConnection.class);
        when(mockConnection.rpc(any(ExecutorService.class), any(String.class), any())).thenReturn(CompletableFuture.completedFuture(null));

        SurrealDriver driver = createDriver(mockConnection);
        driver.ping();

        verify(mockConnection, times(1)).rpc(ForkJoinPool.commonPool(), "ping");
    }
}
