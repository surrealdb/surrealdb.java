package com.surrealdb.client;

import lombok.NonNull;
import meta.tests.SurrealBiDirectionalClientTests;
import meta.tests.SurrealClientGeometryTests;
import meta.tests.SurrealClientGsonTests;
import meta.tests.SurrealClientSpecialOperationsTests;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.TimeUnit;

public class WebSocketSurrealClientTest {

    private static @NotNull SurrealBiDirectionalClient makeClient(@NotNull SurrealClientSettings settings, boolean connect) {
        SurrealBiDirectionalClient client = SurrealWebSocketClient.create(settings);

        if (connect) {
            client.connect(3, TimeUnit.SECONDS);
        }

        return client;
    }

    @Nested
    class SurrealClientTests extends meta.tests.SurrealClientTests {

        @Override
        protected @NonNull SurrealClient createClient(@NonNull SurrealClientSettings settings) {
            return makeClient(settings, true);
        }
    }

    @Nested
    class GeometryTests extends SurrealClientGeometryTests {

        @Override
        protected @NotNull SurrealClient createClient(@NonNull SurrealClientSettings settings) {
            return makeClient(settings, true);
        }
    }

    @Nested
    class GsonTests extends SurrealClientGsonTests {

        @Override
        protected @NotNull SurrealClient createClient(@NonNull SurrealClientSettings settings) {
            return makeClient(settings, true);
        }
    }

    @Nested
    class SpecialOperationsTests extends SurrealClientSpecialOperationsTests {

        @Override
        public @NonNull SurrealClient createClient(@NonNull SurrealClientSettings settings) {
            return makeClient(settings, true);
        }
    }

    @Nested
    class BiDirectionalClientTests extends SurrealBiDirectionalClientTests {

        @Override
        public @NonNull SurrealBiDirectionalClient createClient(@NonNull SurrealClientSettings settings) {
            return makeClient(settings, false);
        }
    }
}
