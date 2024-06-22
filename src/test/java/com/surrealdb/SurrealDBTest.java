package com.surrealdb;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SurrealDBTest {

    @Test
    void surrealdb_websocket() throws SurrealDBException {
        try (Surreal surreal = new Surreal()) {
            // We expected an exception as there is no running server
            RuntimeException e = assertThrows(SurrealDBException.class, () -> {
                surreal.connect("ws://localhost:8000");
            });
            assertTrue(e.getMessage().startsWith("There was an error processing a remote WS request: IO error:"));
        }
    }

    @Test
    void surreal_db_memory() throws SurrealDBException {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            try (Response response = surreal.query("INFO FOR ROOT")) {
                Value value = response.take(0);
                assertTrue(value.isObject());
                Object object = value.getObject();
                assertEquals(object.len(), 2);
                Value ns = object.get("namespaces");
                assertTrue(ns.isObject());
                assertEquals("{  }", ns.toString());
                assertEquals(ns.getObject().len(), 0);
                Value users = object.get("users");
                assertTrue(users.isObject());
                assertEquals(users.getObject().len(), 0);
                assertEquals("{}", users.toPrettyString());

            }
            //
            String sql = "CREATE person:1 SET name = 'Tobie',category = 1, active=true, score=5.0f, tags=['CEO', 'CTO']," +
                    "uuid= u'f8e238f2-e734-47b8-9a16-476b291bd78a', pt = <geometry<point>> { type: \"Point\", coordinates: [-0.118092, 51.509865] };\n" +
                    "SELECT * FROM person;";
            try (Response response = surreal.query(sql)) {
                Value create = response.take(0);
                assertTrue(create.isArray());
                Array createArray = create.getArray();
                assertEquals(createArray.len(), 1);
                assertEquals("[{ active: true, category: 1, id: person:1, name: 'Tobie', pt: (-0.118092, 51.509865), score: 5f, tags: ['CEO', 'CTO'], uuid: 'f8e238f2-e734-47b8-9a16-476b291bd78a' }]", createArray.toString());
                Value select = response.take(1);
                assertTrue(select.isArray());
                Array selectArray = select.getArray();
                assertEquals(selectArray.len(), 1);
                assertEquals("[\n" +
                        "\t{\n" +
                        "\t\tactive: true,\n" +
                        "\t\tcategory: 1,\n" +
                        "\t\tid: person:1,\n" +
                        "\t\tname: 'Tobie',\n" +
                        "\t\tpt: (-0.118092, 51.509865),\n" +
                        "\t\tscore: 5f,\n" +
                        "\t\ttags: [\n" +
                        "\t\t\t'CEO',\n" +
                        "\t\t\t'CTO'\n" +
                        "\t\t],\n" +
                        "\t\tuuid: 'f8e238f2-e734-47b8-9a16-476b291bd78a'\n" +
                        "\t}\n" +
                        "]", selectArray.toPrettyString());
                // Retrieve th fist record
                Value row = selectArray.get(0);
                assertTrue(row.isObject());
                Object rowObject = row.getObject();
                // Check long field
                assertTrue(rowObject.get("category").isLong());
                assertEquals(1, rowObject.get("category").getLong());
                // Check thing field
                assertTrue(rowObject.get("id").isThing());
                assertEquals("person", rowObject.get("id").getThing().getTable());
                assertTrue(rowObject.get("id").getThing().getId().isLong());
                assertEquals(1, rowObject.get("id").getThing().getId().getLong());
                // Check boolean field
                assertTrue(rowObject.get("active").isBoolean());
                assertTrue(rowObject.get("active").getBoolean());
                // Check String field
                assertTrue(rowObject.get("name").isString());
                assertEquals("Tobie", rowObject.get("name").getString());
                // Check Geometry/Point field
                assertTrue(rowObject.get("pt").isGeometry());
                assertEquals(new Point2D.Double(-0.118092, 51.509865), rowObject.get("pt").getGeometry().getPoint());
                // Check double field
                assertTrue(rowObject.get("score").isDouble());
                assertEquals(5.0, rowObject.get("score").getDouble());
                // Check array field
                assertTrue(rowObject.get("tags").isArray());
                assertEquals(2, rowObject.get("tags").getArray().len());
                assertEquals("CEO", rowObject.get("tags").getArray().get(0).getString());
                assertEquals("CTO", rowObject.get("tags").getArray().get(1).getString());
                assertTrue(rowObject.get("uuid").isUuid());
                assertEquals(UUID.fromString("f8e238f2-e734-47b8-9a16-476b291bd78a"), rowObject.get("uuid").getUuid());
                // Outbounded should return none
                Value noneRow = selectArray.get(1);
                assertTrue(noneRow.isNone());

            }
        }
    }
}
