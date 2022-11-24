package com.surrealdb.driver;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.geometry.LineString;
import com.surrealdb.driver.geometry.LinearRing;
import com.surrealdb.driver.geometry.Polygon;
import com.surrealdb.meta.model.City;
import com.surrealdb.meta.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SurrealDriverGeometryTest {

    private static final SurrealTable<City> citiesTable = SurrealTable.of("cities", City.class);

    private SurrealConnection connection;
    private SurrealDriver driver;

    @BeforeEach
    void setup() {
        connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(3);
        driver = SurrealDriver.create(connection);
        driver.signIn(TestUtils.getAuthCredentials());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        CompletableFuture<?>[] cityCreationFutures = City.allCityCenters().stream()
            .map((city) -> driver.createRecordAsync(citiesTable, city))
            .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(cityCreationFutures).join();
    }

    @AfterEach
    void cleanup() {
        driver.deleteAllRecordsInTable(citiesTable);
        connection.disconnect();
    }

    @Test
    void testQueryingPointsInsidePolygon() {
        // A VERY approximate polygon around Japan
        LinearRing selectionExterior = LinearRing.builder()
            .addPointYX(41.41758775838002, 139.36688850517004)
            .addPointYX(32.84100934819944, 128.34142926401432)
            .addPointYX(29.029439014623353, 131.00084055673764)
            .addPointYX(43.78908772017741, 150.52424042309514)
            .addPointYX(45.935268573361704, 139.057803663134)
            .build();
        Polygon selection = Polygon.from(selectionExterior);

        ImmutableMap<String, Object> args = ImmutableMap.of("selection", selection);
        String query = "SELECT * FROM cities WHERE location INSIDE $selection";
        List<City> cities = driver.sqlFirst(query, City.class, args);

        assertEquals(2, cities.size());

        List<String> cityNames = cities.stream().map(City::getName).toList();
        assertTrue(cityNames.contains("Tokyo"), "Tokyo should be in the selection");
        assertTrue(cityNames.contains("Osaka"), "Osaka should be in the selection");
    }
}
