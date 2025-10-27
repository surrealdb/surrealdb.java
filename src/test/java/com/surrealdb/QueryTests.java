package com.surrealdb;

import com.surrealdb.pojos.Dates;
import com.surrealdb.pojos.Partial;
import com.surrealdb.pojos.Person;
import com.surrealdb.pojos.Stats;
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
                    assertEquals(1, createArray.len());
                    assertEquals("[{ id: person:1, name: 'Tobie' }]", createArray.toString());
                }
                { // Check SELECT result
                    final Value select = response.take(1);
                    assertTrue(select.isArray());
                    final Array selectArray = select.getArray();
                    assertEquals(1, selectArray.len());
                    assertEquals("[{ id: person:1, name: 'Tobie' }]", selectArray.toString());
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
                assertEquals(1, createArray.len());
                assertEquals("[{ active: true, category: 1, id: person:1, name: 'Tobie', score: 5f }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(1, selectArray.len());
                assertEquals("[{ active: true, category: 1, id: person:1, name: 'Tobie', score: 5f }]", selectArray.toString());
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
    void queryNull() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "SELECT * FROM ONLY person:unknown";
                final Response response = surreal.query(sql);
                {
                    final Value value = response.take(0);
                    assertTrue(value.isNone());
                }
                {
                    final Person person = response.take(Person.class, 0);
                    assertNull(person);
                }
            }
        }
    }

    @Test
    void queryClassMap() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "CREATE ONLY stats:1 SET statistics.archery = 100, statistics.golf = 70, statistics.running = 120, sessions.example = { duration: 2h, dateTime: d\"2023-07-03T07:18:52Z\"  }, something = true;";
                final Response response = surreal.query(sql);
                final Stats stats = response.take(Stats.class, 0);
                assertEquals(100L, stats.statistics.get("archery"));
                assertEquals(70L, stats.statistics.get("golf"));
                assertEquals(120L, stats.statistics.get("running"));
                final Dates sessions = stats.sessions.get("example");
                assertEquals(2, sessions.duration.toHours());
                assertEquals("2023-07-03T07:18:52Z[UTC]", sessions.dateTime.toString());
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
                assertEquals(1, createArray.len());
                assertEquals("[{ id: person:1, uuid: u'f8e238f2-e734-47b8-9a16-476b291bd78a' }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(1, selectArray.len());
                assertEquals("[{ id: person:1, uuid: u'f8e238f2-e734-47b8-9a16-476b291bd78a' }]", selectArray.toString());
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
                assertEquals(1, createArray.len());
                assertEquals("[{ id: person:1, pt: (-0.118092, 51.509865) }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(1, selectArray.len());
                assertEquals("[{ id: person:1, pt: (-0.118092, 51.509865) }]", selectArray.toString());
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
                assertEquals(1, createArray.len());
                assertEquals("[{ id: person:1, tags: ['CEO', 'CTO'] }]", createArray.toString());
                final Value select = response.take(1);
                assertTrue(select.isArray());
                final Array selectArray = select.getArray();
                assertEquals(1, selectArray.len());
                assertEquals("[{ id: person:1, tags: ['CEO', 'CTO'] }]", selectArray.toString());
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
                    assertEquals("Tobie", create.name);
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
                    assertEquals(3, size);
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
                }
            }
        }
    }

    @Test
    void queryBind() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final HashMap<String,String> map = new HashMap<>();
                map.put("value", "hello");
                map.put("value2", "world");
                final String sql = "RETURN $value;RETURN $value2";
                final Response response = surreal.queryBind(sql,map);
                {
                    final int size = response.size();
                    assertEquals(2, size);
                    final String res1 = response.take(0).getString();
                    assertEquals("hello", res1);
                    final String res2 = response.take(1).getString();
                    assertEquals("world", res2);
                }
            }
        }
    }

    @Test
    void queryBindValues() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                // Create Array and Object
                final String sql1 = "RETURN [1, 2, 3];RETURN { foo: 'bar' }";
                final Response response1 = surreal.query(sql1);
                final HashMap<String, java.lang.Object> map = new HashMap<>();
                map.put("string", "hello_world");
                map.put("long", 25565L);
                map.put("list", Collections.singletonList("item1"));
                map.put("map", Collections.singletonMap("foo", "bar"));
                map.put("array", response1.take(0).getArray());
                map.put("object", response1.take(1).getObject());
                map.put("null", null);
                map.put("uuid", UUID.fromString("f8e238f2-e734-47b8-9a16-476b291bd78a"));
                final String sql2 = "RETURN [$string, $long, $list, $map, $array, $object, $null, $uuid]";
                final Response response2 = surreal.queryBind(sql2,map);
                {
                    final Array results = response2.take(0).getArray();
                    assertEquals(8, results.len());
                    final String res1 = results.get(0).getString();
                    assertEquals("hello_world", res1);
                    final long res2 = results.get(1).getLong();
                    assertEquals(25565L, res2);
                    final Array res3 = results.get(2).getArray();
                    assertEquals(1, res3.len());
                    assertEquals("item1", res3.get(0).getString());
                    final Object res4 = results.get(3).getObject();
                    assertEquals(1, res4.len());
                    assertEquals("bar", res4.get("foo").getString());
                    final Array res5 = results.get(4).getArray();
                    assertEquals("[1, 2, 3]", res5.toString());
                    final Object res6 = results.get(5).getObject();
                    assertEquals("{ foo: 'bar' }", res6.toString());
                    final Value res7 = results.get(6);
                    assertTrue(res7.isNull());
                    final UUID res8 = results.get(7).getUuid();
                    assertEquals("f8e238f2-e734-47b8-9a16-476b291bd78a", res8.toString());
                }
            }
        }
    }

    @Test
    void queryWithValueMut() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final HashMap<String,ValueMut> map = new HashMap<>();
                map.put("null", ValueMut.createNull());
                map.put("none", ValueMut.createNone());
                final String sql = "RETURN $null;RETURN $none";
                final Response response = surreal.queryBind(sql,map);
                {
                    final int size = response.size();
                    assertEquals(2, size);
                    final Value res1 = response.take(0);
                    assertTrue(res1.isNull());
                    final Value res2 = response.take(1);
                    assertTrue(res2.isNone());
                }
            }
        }
    }

    @Test
    void queryValue() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final String sql = "RETURN { inner: true }";
                final Response response = surreal.query(sql);
                final Partial partial = response.take(Partial.class, 0);
                assertTrue(partial.inner.getBoolean());
            }
        }
    }
}
