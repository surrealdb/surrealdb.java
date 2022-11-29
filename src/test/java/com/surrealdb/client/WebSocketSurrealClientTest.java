package com.surrealdb.client;

import lombok.NonNull;
import meta.tests.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.TimeUnit;

public class WebSocketSurrealClientTest extends SurrealClientTests {

    private static @NotNull SurrealBiDirectionalClient makeClient(@NotNull SurrealClientSettings settings, boolean connect) {
        SurrealBiDirectionalClient client = SurrealWebSocketClient.create(settings);

        if (connect) {
            client.connect(3, TimeUnit.SECONDS);
        }

        return client;
    }

    @Override
    protected SurrealClient createClient(SurrealClientSettings settings) {
        return makeClient(settings, true);
    }

    @Nested
    class GeometryTests extends SurrealClientGeometryTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return makeClient(settings, true );
        }
    }

    @Nested
    class GsonTests extends SurrealClientGsonTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return makeClient(settings, true);
        }
    }

    @Nested
    class SpecialOperationsTests extends SurrealClientSpecialOperationsTests {

        @Override
        public SurrealClient createClient(SurrealClientSettings settings) {
            return makeClient(settings, true);
        }
    }

    @Nested
    class BiDirectionalClientTests extends SurrealBiDirectionalClientTests {

        @Override
        public @NonNull SurrealBiDirectionalClient createClient(SurrealClientSettings settings) {
            return makeClient(settings, false);
        }
    }
}
