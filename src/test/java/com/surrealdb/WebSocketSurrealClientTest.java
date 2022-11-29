package com.surrealdb;

import meta.tests.SurrealClientGeometryTests;
import meta.tests.SurrealClientGsonTests;
import meta.tests.SurrealClientSpecialOperationsTests;
import meta.tests.SurrealClientTests;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;

public class WebSocketSurrealClientTest extends SurrealClientTests {

    @Override
    protected SurrealClient createClient(SurrealClientSettings settings) {
        return WebSocketSurrealClient.create(settings);
    }

    @Nested
    class GeometryTests extends SurrealClientGeometryTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return WebSocketSurrealClient.create(settings);
        }
    }

    @Nested
    class GsonTests extends SurrealClientGsonTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return WebSocketSurrealClient.create(settings);
        }
    }

    @Nested
    class SpecialOperationsTests extends SurrealClientSpecialOperationsTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return WebSocketSurrealClient.create(settings);
        }
    }
}
