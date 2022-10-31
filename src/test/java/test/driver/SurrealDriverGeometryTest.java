package test.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.geometry.Line;
import com.surrealdb.driver.model.geometry.Point;
import com.surrealdb.driver.model.geometry.Polygon;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.GenericGeometryContainer;

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
    void testGeometrySerialization() {
        GenericGeometryContainer expected = new GenericGeometryContainer("St. Louis");

        // The Arch
        expected.setPoint(Point.fromLatitudeLongitude(38.624813, -90.184938));

        // Mississippi River
        expected.setLine(Line.builder()
            .addPointLatitudeLongitude(38.664938, -90.183063) // McKinley Bridge
            .addPointLatitudeLongitude(38.645938, -90.178562) // Stan Musial Veterans Memorial Bridge
            .addPointLatitudeLongitude(38.628937, -90.178812) // Eads Bridge
            .addPointLatitudeLongitude(38.618063, -90.182563) // That one bridge near the Arch
            .build()
        );

        // Forest Park
        expected.setPolygon(Polygon.builder()
            .addOuterRingPointLatitudeLongitude(38.632662, -90.304484) // South-west corner
            .addOuterRingPointLatitudeLongitude(38.628888, -90.264734) // South-east corner
            .addOuterRingPointLatitudeLongitude(38.643937, -90.265187) // North-east corner
            .addOuterRingPointLatitudeLongitude(38.647937, -90.304937) // North-west corner
            .build()
        );

        GenericGeometryContainer actual = driver.create("geometry:stl", expected);

        assertEquals(expected.getPoint(), actual.getPoint());
        assertEquals(expected.getLine(), actual.getLine());
        assertEquals(expected.getPolygon(), actual.getPolygon());
        assertEquals(expected, actual);
    }
}
