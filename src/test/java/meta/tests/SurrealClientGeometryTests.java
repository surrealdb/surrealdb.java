package meta.tests;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.SurrealClient;
import com.surrealdb.SurrealClientSettings;
import com.surrealdb.SurrealTable;
import com.surrealdb.geometry.LinearRing;
import com.surrealdb.geometry.Polygon;
import meta.model.City;
import meta.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SurrealClientGeometryTests {

    private static final SurrealTable<City> citiesTable = SurrealTable.of("cities", City.class);

    private SurrealClient client;

    protected abstract @NotNull SurrealClient createClient(SurrealClientSettings settings);

    @BeforeEach
    void setup() {
        client = createClient(TestUtils.getClientSettings());
        client.connect(3, TimeUnit.SECONDS);
        client.signIn(TestUtils.getAuthCredentials());
        client.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        CompletableFuture<?>[] cityCreationFutures = City.allCityCenters().stream()
            .map((city) -> client.createRecordAsync(citiesTable, city))
            .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(cityCreationFutures).join();
    }

    @AfterEach
    void cleanup() {
        client.deleteAllRecordsInTable(citiesTable);
        client.disconnect();
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
        List<City> cities = client.sqlFirst(query, City.class, args);

        assertEquals(2, cities.size());

        List<String> cityNames = cities.stream().map(City::getName).toList();
        assertTrue(cityNames.contains("Tokyo"), "Tokyo should be in the selection");
        assertTrue(cityNames.contains("Osaka"), "Osaka should be in the selection");
    }
}
