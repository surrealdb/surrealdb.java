package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class QueryTests {

    @Test
    void query() throws SurrealException {
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
    void queryPrimitiveFields() throws SurrealException {
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
    void queryUuid() throws SurrealException {
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
                assertEquals("[{ id: person:1, uuid: u'f8e238f2-e734-47b8-9a16-476b291bd78a' }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(selectArray.len(), 1);
                assertEquals("[\n" +
                    "\t{\n" +
                    "\t\tid: person:1,\n" +
                    "\t\tuuid: u'f8e238f2-e734-47b8-9a16-476b291bd78a'\n" +
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
    void queryGeometry() throws SurrealException {
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

    @Test
    void queryArray() throws SurrealException {
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
                    // Iterator checker
                    final Consumer<Iterator<Value>> iteratorCheck = (iter) -> {
                        assertTrue(iter.hasNext());
                        assertEquals("CEO", iter.next().getString());
                        assertTrue(iter.hasNext());
                        assertEquals("CTO", iter.next().getString());
                        assertFalse(iter.hasNext());
                    };
                    // Check array iterator
                    iteratorCheck.accept((array.iterator()));
                    // Check array sync iterator
                    iteratorCheck.accept((array.synchronizedIterator()));
                }
            }
        }
    }

    @Test
    void queryObject() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "RETURN { name: 'Tobie', active: true, category: 1 };";
                final Response response = surreal.query(sql);
                final Value res = response.take(0);
                assertTrue(res.isObject());
                final Object obj = res.getObject();
                assertEquals(3, obj.len());
                // Iterator checker
                final Consumer<Iterator<Entry>> objectCheck = (iter) -> {
                    { // Check active
                        assertTrue(iter.hasNext());
                        final Entry active = iter.next();
                        assertEquals("active", active.getKey());
                        assertTrue(active.getValue().getBoolean());
                    }
                    { // Check category
                        assertTrue(iter.hasNext());
                        final Entry category = iter.next();
                        assertEquals("category", category.getKey());
                        assertEquals(1, category.getValue().getLong());
                    }
                    { // Check name
                        assertTrue(iter.hasNext());
                        final Entry name = iter.next();
                        assertEquals("name", name.getKey());
                        assertEquals("Tobie", name.getValue().getString());
                        assertFalse(iter.hasNext());
                    }
                };
                // Check object iterator
                objectCheck.accept(obj.iterator());
                // Check object sync iterator
                objectCheck.accept(obj.synchronizedIterator());
            }
        }
    }

    @Test
    void queryClassValueIterator() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql =
                    "CREATE person:1 SET active=true, category=1, tags=['CEO', 'CTO'], emails=[{address: 'tobie@example.com', name: { first:'Tobie', last: 'Foo' }}];" +
                        "CREATE person:2 SET active=true, category=2, tags=['COO'], emails=[{address: 'jaime@example.com', name: { first:'Jaime', last:'Bar' }}];" +
                        "SELECT * FROM person;";
                final Response response = surreal.query(sql);
                // Iterator over the SELECT
                final Value select = response.take(2);
                final Array results = select.getArray();
                final Iterator<Person> iterator = results.iterator(Person.class);
                {
                    assertTrue(iterator.hasNext());
                    final Person p = iterator.next();
                    assertEquals(new RecordId("person", 1), p.id);
                    assertEquals(1, p.category);
                    assertTrue(p.active);
                    assertEquals(Arrays.asList("CEO", "CTO"), p.tags);
                    assertEquals("Tobie", p.emails.get(0).name.first);
                    assertEquals("Foo", p.emails.get(0).name.last);
                    assertEquals("tobie@example.com", p.emails.get(0).address);
                }
                {
                    assertTrue(iterator.hasNext());
                    final Person p = iterator.next();
                    assertEquals(new RecordId("person", 2), p.id);
                    assertEquals(2, p.category);
                    assertTrue(p.active);
                    assertEquals(Collections.singletonList("COO"), p.tags);
                    assertEquals("Jaime", p.emails.get(0).name.first);
                    assertEquals("Bar", p.emails.get(0).name.last);
                    assertEquals("jaime@example.com", p.emails.get(0).address);
                }
                assertFalse(iterator.hasNext());
            }
        }
    }

    @Test
    void queryClass() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "CREATE ONLY person:1 SET name = 'Tobie';";
                final Response response = surreal.query(sql);
                {
                    final Person create = response.take(Person.class, 0);
                    assertEquals(create.name, "Tobie");
                }
            }
        }
    }

    @Test
    void queryResponseSize() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "RETURN 'one'; RETURN 'two'; RETURN 'three'";
                final Response response = surreal.query(sql);
                {
                    final int size = response.size();
                    assertEquals(size, 3);
                }
            }
        }
    }

    @Test
    void queryBytes() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "return <bytes>\"hello world\"";
                final Response response = surreal.query(sql);
                {
                    final byte[] bytes = response.take(0).getBytes();
                    assertArrayEquals("hello world".getBytes(), bytes);

    @Test
    void queryWithValue() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final HashMap<String,ValueMut> map = new HashMap<String,ValueMut>();
                final ValueMut value = ValueMut.createLong(1000);
                map.put("value", value);
                final String sql = "RETURN $value;";
                final Response response = surreal.queryWithValue(sql,map);
                {
                    final int size = response.size();
                    assertEquals(size, 1);
                    final long val = response.take(0).getLong();
                    assertEquals(val, 1000);
                }
            }
        }
    }
}
