package meta.tests;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.client.SurrealClient;
import com.surrealdb.client.SurrealClientSettings;
import com.surrealdb.geometry.GeometryCollection;
import com.surrealdb.geometry.LinearRing;
import com.surrealdb.geometry.Point;
import com.surrealdb.geometry.Polygon;
import com.surrealdb.types.SurrealRecord;
import com.surrealdb.types.SurrealTable;
import lombok.EqualsAndHashCode;
import lombok.Value;
import meta.model.City;
import meta.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SurrealClientGeometryTests {

    private static final SurrealTable<City> citiesTable = SurrealTable.of("cities", City.class);

    private SurrealClient client;

    protected abstract @NotNull SurrealClient createClient(@NotNull SurrealClientSettings settings);

    @BeforeEach
    void setup() {
        client = createClient(TestUtils.getClientSettings());
        client.signIn(TestUtils.getAuthCredentials());
        client.setNamespaceAndDatabase(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @AfterEach
    void cleanup() {
        client.deleteAllRecordsInTable(citiesTable);
    }

    void createCities() {
        CompletableFuture<?>[] cityCreationFutures = City.allCityCenters().stream()
            .map(city -> client.createRecordAsync(citiesTable, city))
            .toList()
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(cityCreationFutures).join();
    }

    @Test
    void testQueryingPointsInsidePolygon() {
        createCities();

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

    @Value
    @EqualsAndHashCode(callSuper = false)
    static class GeometryCollectionContainer extends SurrealRecord {

        @NotNull GeometryCollection collection;

    }

    @Test
    void testAddingGeometryCollectionContainingGeometryCollection() {
        GeometryCollection collection1 = GeometryCollection.from(Point.fromXY(1, 2));
        GeometryCollection collection2 = GeometryCollection.from(collection1);

        GeometryCollectionContainer container = new GeometryCollectionContainer(collection2);

        SurrealTable<GeometryCollectionContainer> table = SurrealTable.of("geometry_collection_containers", GeometryCollectionContainer.class);
        client.createRecord(table, "test", container);
        client.retrieveRecord(table, "test");
        client.deleteAllRecordsInTable(table);
    }
}
