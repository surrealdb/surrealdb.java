package test.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.geometry.SurrealLineString;
import com.surrealdb.driver.model.geometry.SurrealPoint;
import com.surrealdb.driver.model.geometry.SurrealPolygon;
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

        expected.setPoint(SurrealPoint.fromLatitudeLongitude(38.624813, -90.184938));
//         Mississippi River
        expected.setLineString(new SurrealLineString(
            // McKinley Bridge
            SurrealPoint.fromLatitudeLongitude(38.664938, -90.183063),
            // Stan Musial Veterans Memorial Bridge
            SurrealPoint.fromLatitudeLongitude(38.645938, -90.178562),
            // Eads Bridge
            SurrealPoint.fromLatitudeLongitude(38.628937, -90.178812),
            // That one bridge near the Arch
            SurrealPoint.fromLatitudeLongitude(38.618063, -90.182563)));

        // Forest Park
        expected.setPolygon(SurrealPolygon.fromOuterRing(
            // South-west corner
            SurrealPoint.fromLatitudeLongitude(38.632662, -90.304484),
            // South-east corner
            SurrealPoint.fromLatitudeLongitude(38.628888, -90.264734),
            // North-east corner
            SurrealPoint.fromLatitudeLongitude(38.643937, -90.265187),
            // North-west corner
            SurrealPoint.fromLatitudeLongitude(38.647937, -90.304937)
        ));


        GenericGeometryContainer actual = driver.create("geometry:stl", expected);

        assertEquals(expected.getPoint(), actual.getPoint());
        assertEquals(expected.getLineString(), actual.getLineString());
        assertEquals(expected.getPolygon(), actual.getPolygon());
        assertEquals(expected, actual);
    }
}
