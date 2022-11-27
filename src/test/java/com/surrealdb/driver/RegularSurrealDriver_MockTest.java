package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.meta.SurrealDriver_MockTest;

@SuppressWarnings("NewClassNamingConvention")
public class RegularSurrealDriver_MockTest extends SurrealDriver_MockTest {

    @Override
    protected SurrealDriver createDriver(SurrealConnection connection) {
        return RegularSurrealDriver.create(connection, SurrealDriverSettings.DEFAULT);
    }
}
