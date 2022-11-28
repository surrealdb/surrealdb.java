package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.meta.driver.*;
import org.junit.jupiter.api.Nested;

public class RegularSurrealDriverTest extends SurrealDriverTests {

    @Override
    protected SurrealDriver createDriver(SurrealConnection connection, SurrealDriverSettings settings) {
        return RegularSurrealDriver.create(connection, settings);
    }

    @Nested
    class MockTests extends SurrealDriverMockTests {

        @Override
        protected SurrealDriver createDriver(SurrealConnection connection) {
            return RegularSurrealDriver.create(connection, SurrealDriverSettings.DEFAULT);
        }
    }

    @Nested
    class GeometryTests extends SurrealDriverGeometryTests {

        @Override
        protected SurrealDriver createDriver(SurrealConnection connection, SurrealDriverSettings settings) {
            return RegularSurrealDriver.create(connection, settings);
        }
    }

    @Nested
    class GsonTests extends SurrealDriverGsonTests {

        @Override
        protected SurrealDriver createDriver(SurrealConnection connection, SurrealDriverSettings settings) {
            return RegularSurrealDriver.create(connection, settings);
        }
    }

    @Nested
    class SpecialOperationsTests extends SurrealDriverSpecialOperationsTests {

        @Override
        protected SurrealDriver createDriver(SurrealConnection connection, SurrealDriverSettings settings) {
            return RegularSurrealDriver.create(connection, settings);
        }
    }
}
