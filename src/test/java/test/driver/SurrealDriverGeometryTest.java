package test.driver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SurrealDriver;
import com.surrealdb.driver.SurrealTable;
import com.surrealdb.driver.geometry.LineString;
import com.surrealdb.driver.geometry.LinearRing;
import com.surrealdb.driver.geometry.Point;
import com.surrealdb.driver.geometry.Polygon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.City;
import test.driver.model.GeoContainer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SurrealDriverGeometryTest {

    private static final ImmutableList<City> CITIES = ImmutableList.of(
        new City(Point.fromGeoHash("xn76urx6"), "Tokyo", 37),
        new City(Point.fromGeoHash("ttnghcy0"), "Delhi", 28),
        new City(Point.fromGeoHash("wtw3sjq6"), "Shanghai", 25),
        new City(Point.fromGeoHash("6gyf4bdx"), "São Paulo", 21),
        new City(Point.fromGeoHash("9g3w81t7"), "Mexico City", 21),
        new City(Point.fromGeoHash("stq4yv3j"), "Cairo", 20),
        new City(Point.fromGeoHash("te7ud2ev"), "Mumbai", 20),
        new City(Point.fromGeoHash("wx4g0bm6"), "Beijing", 20),
        new City(Point.fromGeoHash("wh0r3qs3"), "Dhaka", 20),
        new City(Point.fromGeoHash("xn0m77v9"), "Osaka", 19),
        new City(Point.fromGeoHash("dr5regw3"), "New York", 19),
        new City(Point.fromGeoHash("tkrtkvgh"), "Karachi", 19),
        new City(Point.fromGeoHash("69y7pkxf"), "Buenos Aires", 19),
        new City(Point.fromGeoHash("wm7b0tu3"), "Chongqing", 19),
        new City(Point.fromGeoHash("sxk973m6"), "Istanbul", 18)
    );

    private static final SurrealTable<GeoContainer> geometryTable = SurrealTable.of("geometry", GeoContainer.class);
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

        CompletableFuture<?>[] cityCreationFutures = CITIES.stream()
            .map((city) -> driver.createRecordAsync(citiesTable, city))
            .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(cityCreationFutures).join();
    }

    @AfterEach
    void cleanup() {
        driver.deleteAllRecordsInTable(geometryTable);
        driver.deleteAllRecordsInTable(citiesTable);
        connection.disconnect();
    }

    @Test
    void testQueryingPointsInsidePolygon() {
        // A VERY approximate polygon around Japan
        LinearRing selectionExterior = LineString.builder()
            .addPointYX(41.41758775838002, 139.36688850517004)
            .addPointYX(32.84100934819944, 128.34142926401432)
            .addPointYX(29.029439014623353, 131.00084055673764)
            .addPointYX(43.78908772017741, 150.52424042309514)
            .addPointYX(45.935268573361704, 139.057803663134)
            .buildLinearRing();
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
