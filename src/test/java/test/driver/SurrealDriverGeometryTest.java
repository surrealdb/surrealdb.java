package test.driver;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.geometry.Line;
import com.surrealdb.driver.model.geometry.Point;
import com.surrealdb.driver.model.geometry.Polygon;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.connection.gson.GsonTestUtils;
import test.driver.model.GeoContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SurrealDriverGeometryTest {

    private SyncSurrealDriver driver;

    @BeforeEach
    void setup() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(3);
        driver = new SyncSurrealDriver(connection);
        driver.signInAsRootUser(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @AfterEach
    void cleanup() {
        driver.delete("geometry");
        driver.getSurrealConnection().disconnect();
    }

    @Test
    void testQueryingPointsInsidePolygon() {
        GeoContainer point1 = new GeoContainer("Point 1");
        // Point inside polygon
        point1.setPoint(Point.fromLatitudeLongitude(38.638688,-90.291562));

        GeoContainer point2 = new GeoContainer("Point 2");
        point2.setPoint(Point.fromLatitudeLongitude(0, 0));

        driver.create("geometry", point1);
        driver.create("geometry", point2);

        // Forest park in STL
        Line polygonExterior = Line.builder()
            .addPointLatitudeLongitude(38.632662, -90.304484) // South-west corner
            .addPointLatitudeLongitude(38.628888, -90.264734) // South-east corner
            .addPointLatitudeLongitude(38.643937, -90.265187) // North-east corner
            .addPointLatitudeLongitude(38.647937, -90.304937) // North-west corner
            .addPointLatitudeLongitude(38.632662, -90.304484) // South-west corner (again)
            .build();
        String polygon = GsonTestUtils.serializeToJsonElement(Polygon.from(polygonExterior)).toString();
        driver.setConnectionWideParameter("polygon", polygon);

        List<QueryResult<GeoContainer>> queryResults = driver.query("SELECT * FROM geometry WHERE point INSIDE " + polygon + ";", ImmutableMap.of(), GeoContainer.class);

        System.out.println(queryResults);

        assertEquals(queryResults.size(), 1);
        List<GeoContainer> firstQueryResult = queryResults.get(0).getResult();
        assertEquals(1, firstQueryResult.size());
        assertEquals(firstQueryResult.get(0).getName(), "Point 1");
    }
}
