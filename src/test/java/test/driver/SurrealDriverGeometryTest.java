package test.driver;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SurrealTable;
import com.surrealdb.driver.SurrealSyncDriver;
import com.surrealdb.driver.geometry.Line;
import com.surrealdb.driver.geometry.Point;
import com.surrealdb.driver.geometry.Polygon;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.GeoContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SurrealDriverGeometryTest {

    private static final SurrealTable<GeoContainer> geometryTable = SurrealTable.create("geometry", GeoContainer.class);

    private SurrealConnection connection;
    private SurrealSyncDriver driver;

    @BeforeEach
    void setup() {
        connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(3);
        driver = new SurrealSyncDriver(connection);
        driver.signIn(TestUtils.getAuthCredentials());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @AfterEach
    void cleanup() {
        driver.deleteRecords(geometryTable);
        connection.disconnect();
    }

    @Test
    void testQueryingPointsInsidePolygon() {
        GeoContainer point1 = new GeoContainer("Point 1");
        // Point inside polygon
        point1.setPoint(Point.fromYX(38.638688,-90.291562));

        GeoContainer point2 = new GeoContainer("Point 2");
        point2.setPoint(Point.fromYX(0, 0));

        driver.create(geometryTable, point1);
        driver.create(geometryTable, point2);

        // Forest park in STL
        Line selectionExterior = Line.builder()
            .addPointYX(38.632662, -90.304484) // South-west corner
            .addPointYX(38.628888, -90.264734) // South-east corner
            .addPointYX(38.643937, -90.265187) // North-east corner
            .addPointYX(38.647937, -90.304937) // North-west corner
            .addPointYX(38.632662, -90.304484) // South-west corner (again)
            .build();
        Polygon selection = Polygon.from(selectionExterior);

        ImmutableMap<String, Object> args = ImmutableMap.of("selection", selection);
        String query = "SELECT * FROM geometry WHERE point INSIDE $selection LIMIT 1;";
        Optional<GeoContainer> queryResults = driver.querySingle(query, GeoContainer.class, args);

        assertTrue(queryResults.isPresent());
        assertEquals("Point 1", queryResults.get().getName());
    }
}
