package com.surrealdb;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.Iterator;
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
    void surrealdb_info_for_root() throws SurrealDBException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final Response response = surreal.query("INFO FOR ROOT");
                final Value value = response.take(0);
                assertTrue(value.isObject());
                final Object object = value.getObject();
                assertEquals(object.len(), 2);
                {
                    final Value ns = object.get("namespaces");
                    assertTrue(ns.isObject());
                    assertEquals("{  }", ns.toString());
                    assertEquals(ns.getObject().len(), 0);
                }
                {
                    final Value users = object.get("users");
                    assertTrue(users.isObject());
                    assertEquals(users.getObject().len(), 0);
                    assertEquals("{}", users.toPrettyString());
                }
            }
        }
    }

    @Test
    void surrealdb_query() throws SurrealDBException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            //
            {
                final String sql = "CREATE person:1 SET name = 'Tobie';" +
                        "SELECT * FROM person;";
                final Response response = surreal.query(sql);
                { // Check CREATE result
                    final Value create = response.take(0);
                    assertTrue(create.isArray());
                    final Array createArray = create.getArray();
                    assertEquals(createArray.len(), 1);
                    assertEquals("[{ id: person:1, name: 'Tobie' }]", createArray.toString());
                }
                { // Check SELECT result
                    final Value select = response.take(1);
                    assertTrue(select.isArray());
                    final Array selectArray = select.getArray();
                    assertEquals(selectArray.len(), 1);
                    assertEquals("[\n" +
                            "\t{\n" +
                            "\t\tid: person:1,\n" +
                            "\t\tname: 'Tobie'\n" +
                            "\t}\n" +
                            "]", selectArray.toPrettyString());
                    { // Retrieve the fist record
                        final Value row = selectArray.get(0);
                        assertTrue(row.isObject());
                        final Object rowObject = row.getObject();

                        { // Check thing field
                            assertTrue(rowObject.get("id").isThing());
                            assertEquals("person", rowObject.get("id").getThing().getTable());
                            assertTrue(rowObject.get("id").getThing().getId().isLong());
                            assertEquals(1, rowObject.get("id").getThing().getId().getLong());
                        }
                        { // Check String field
                            assertTrue(rowObject.get("name").isString());
                            assertEquals("Tobie", rowObject.get("name").getString());
                        }
                    }
                    { // Outbound get should return none
                        final Value noneRow = selectArray.get(1);
                        assertTrue(noneRow.isNone());
                    }
                }
            }
        }
    }

    @Test
    void surrealdb_query_primitive_fields() throws SurrealDBException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            //
            {
                final String sql = "CREATE person:1 SET name = 'Tobie',category = 1, active=true, score=5.0f;" +
                        "SELECT * FROM person;";
                final Response response = surreal.query(sql);
                final Value create = response.take(0);
                assertTrue(create.isArray());
                final Array createArray = create.getArray();
                assertEquals(createArray.len(), 1);
                assertEquals("[{ active: true, category: 1, id: person:1, name: 'Tobie', score: 5f }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(selectArray.len(), 1);
                assertEquals("[\n" +
                        "\t{\n" +
                        "\t\tactive: true,\n" +
                        "\t\tcategory: 1,\n" +
                        "\t\tid: person:1,\n" +
                        "\t\tname: 'Tobie',\n" +
                        "\t\tscore: 5f\n" +
                        "\t}\n" +
                        "]", selectArray.toPrettyString());
                // Retrieve the fist record
                final Value row = selectArray.get(0);
                assertTrue(row.isObject());
                final Object rowObject = row.getObject();

                { // Check long field
                    assertTrue(rowObject.get("category").isLong());
                    assertEquals(1, rowObject.get("category").getLong());
                }
                { // Check thing field
                    assertTrue(rowObject.get("id").isThing());
                    assertEquals("person", rowObject.get("id").getThing().getTable());
                    assertTrue(rowObject.get("id").getThing().getId().isLong());
                    assertEquals(1, rowObject.get("id").getThing().getId().getLong());
                }
                { // Check boolean field
                    assertTrue(rowObject.get("active").isBoolean());
                    assertTrue(rowObject.get("active").getBoolean());
                }
                { // Check String field
                    assertTrue(rowObject.get("name").isString());
                    assertEquals("Tobie", rowObject.get("name").getString());
                }
                { // Check double field
                    assertTrue(rowObject.get("score").isDouble());
                    assertEquals(5.0, rowObject.get("score").getDouble());
                }
            }
        }
    }

    @Test
    void surrealdb_query_array() throws SurrealDBException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "CREATE person:1 SET tags=['CEO', 'CTO'];" +
                        "SELECT * FROM person;";
                final Response response = surreal.query(sql);
                final Value create = response.take(0);
                assertTrue(create.isArray());
                final Array createArray = create.getArray();
                assertEquals(createArray.len(), 1);
                assertEquals("[{ id: person:1, tags: ['CEO', 'CTO'] }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(selectArray.len(), 1);
                assertEquals("[\n" +
                        "\t{\n" +
                        "\t\tid: person:1,\n" +
                        "\t\ttags: [\n" +
                        "\t\t\t'CEO',\n" +
                        "\t\t\t'CTO'\n" +
                        "\t\t]\n" +
                        "\t}\n" +
                        "]", selectArray.toPrettyString());
                // Retrieve the fist record
                final Value row = selectArray.get(0);
                assertTrue(row.isObject());
                final Object rowObject = row.getObject();

                {// Check array field
                    assertTrue(rowObject.get("tags").isArray());
                    final Array array = rowObject.get("tags").getArray();
                    assertEquals(2, array.len());
                    assertEquals("CEO", array.get(0).getString());
                    assertEquals("CTO", array.get(1).getString());
                    { // Check array iterator
                        final Iterator<Value> iter = array.iterator();
                        assertTrue(iter.hasNext());
                        assertEquals("CEO", iter.next().getString());
                        assertTrue(iter.hasNext());
                        assertEquals("CTO", iter.next().getString());
                        assertFalse(iter.hasNext());
                    }
                    { // Check array sync iterator
                        final Iterator<Value> iter = array.synchronizedIterator();
                        assertTrue(iter.hasNext());
                        assertEquals("CEO", iter.next().getString());
                        assertTrue(iter.hasNext());
                        assertEquals("CTO", iter.next().getString());
                        assertFalse(iter.hasNext());
                    }
                }
            }
        }
    }

    @Test
    void surrealdb_query_uuid() throws SurrealDBException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "CREATE person:1 SET uuid= u'f8e238f2-e734-47b8-9a16-476b291bd78a';\n" +
                        "SELECT * FROM person;";
                final Response response = surreal.query(sql);
                final Value create = response.take(0);
                assertTrue(create.isArray());
                final Array createArray = create.getArray();
                assertEquals(createArray.len(), 1);
                assertEquals("[{ id: person:1, uuid: 'f8e238f2-e734-47b8-9a16-476b291bd78a' }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(selectArray.len(), 1);
                assertEquals("[\n" +
                        "\t{\n" +
                        "\t\tid: person:1,\n" +
                        "\t\tuuid: 'f8e238f2-e734-47b8-9a16-476b291bd78a'\n" +
                        "\t}\n" +
                        "]", selectArray.toPrettyString());
                // Retrieve the fist record
                final Value row = selectArray.get(0);
                assertTrue(row.isObject());
                final Object rowObject = row.getObject();
                // Check UUID
                assertTrue(rowObject.get("uuid").isUuid());
                assertEquals(UUID.fromString("f8e238f2-e734-47b8-9a16-476b291bd78a"), rowObject.get("uuid").getUuid());
            }
        }
    }

    @Test
    void surrealdb_query_geometry() throws SurrealDBException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            //
            {
                final String sql = "CREATE person:1 SET pt = <geometry<point>> { type: \"Point\", coordinates: [-0.118092, 51.509865] };\n" +
                        "SELECT * FROM person;";
                final Response response = surreal.query(sql);
                final Value create = response.take(0);
                assertTrue(create.isArray());
                final Array createArray = create.getArray();
                assertEquals(createArray.len(), 1);
                assertEquals("[{ id: person:1, pt: (-0.118092, 51.509865) }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(selectArray.len(), 1);
                assertEquals("[\n" +
                        "\t{\n" +
                        "\t\tid: person:1,\n" +
                        "\t\tpt: (-0.118092, 51.509865)\n" +
                        "\t}\n" +
                        "]", selectArray.toPrettyString());
                // Retrieve the fist record
                final Value row = selectArray.get(0);
                assertTrue(row.isObject());
                final Object rowObject = row.getObject();
                {// Check Geometry/Point field
                    assertTrue(rowObject.get("pt").isGeometry());
                    assertEquals(new Point2D.Double(-0.118092, 51.509865), rowObject.get("pt").getGeometry().getPoint());
                }
            }
        }
    }
}
