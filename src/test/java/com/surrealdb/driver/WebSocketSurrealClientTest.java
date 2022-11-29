package com.surrealdb.driver;

import com.surrealdb.SurrealClient;
import com.surrealdb.SurrealClientSettings;
import com.surrealdb.WebSocketSurrealClient;
import com.surrealdb.meta.driver.SurrealDriverGeometryTests;
import com.surrealdb.meta.driver.SurrealDriverGsonTests;
import com.surrealdb.meta.driver.SurrealDriverSpecialOperationsTests;
import com.surrealdb.meta.driver.SurrealDriverTests;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;

public class WebSocketSurrealClientTest extends SurrealDriverTests {

    @Override
    protected SurrealClient createClient(SurrealClientSettings settings) {
        return WebSocketSurrealClient.create(settings);
    }

    @Nested
    class GeometryTests extends SurrealDriverGeometryTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return WebSocketSurrealClient.create(settings);
        }
    }

    @Nested
    class GsonTests extends SurrealDriverGsonTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return WebSocketSurrealClient.create(settings);
        }
    }

    @Nested
    class SpecialOperationsTests extends SurrealDriverSpecialOperationsTests {

        @Override
        protected @NotNull SurrealClient createClient(SurrealClientSettings settings) {
            return WebSocketSurrealClient.create(settings);
        }
    }
}
