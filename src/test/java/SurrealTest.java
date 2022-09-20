import com.surrealdb.java.Surreal;
import com.surrealdb.java.model.QueryResult;
import lombok.extern.slf4j.Slf4j;
import model.Person;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SurrealTest {

    private Surreal surreal;
    private String personId;

    @BeforeAll
    public void init(){
        surreal = new Surreal("172.18.0.2", 8000);
        personId = System.currentTimeMillis()+"";
    }

    @Test
    @Order(1)
    public void testSignIn() {
        surreal.signIn("root", "root");
    }

    @Test
    @Order(2)
    public void testUse() {
        surreal.use("test", "test");
    }

    @Test
    @Order(3)
    public void testLet() {
        surreal.let("someKey", "someValue");
    }

    @Test
    @Order(4)
    public void testCreateNoId() {
        Person person = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
        person = surreal.create("person", person);
        log.info("new person {}", person);
    }

    @Test
    @Order(5)
    public void testCreateWithId() {
        Person person = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
            person = surreal.create("person:"+personId, person);
        log.info("new person {}", person);
    }

    @Test
    @Order(6)
    public void testQuery() {
        Map<String, String> args = new HashMap<>();
        args.put("firstName", "Tobie");
        List<QueryResult<Person>> actual = surreal.query("select * from person where name.first = $firstName", args, Person.class);

        assertEquals(1, actual.size());
        assertEquals("OK", actual.get(0).getStatus());
        assertTrue(actual.get(0).getResult().size() >= 2);
    }

    @Test
    @Order(7)
    public void testSelectExists() {
        Person expected = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
        expected.setId("person:"+personId);

        List<Person> actual = surreal.select("person:"+personId, Person.class);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    @Order(8)
    public void testSelectDoesNotExist() {
        List<Person> actual = surreal.select("person:500", Person.class);
        assertEquals(0, actual.size());
    }

    @Test
    @Order(9)
    public void testUpdateOne() {
        Person expected = new Person("Founder", "Tobie", "Morgan Hitchcock", true);
        expected.setId("person:"+personId);

        List<Person> actual = surreal.update("person:"+personId, expected);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    @Order(10)
    public void testUpdateAll() {
        Person expected = new Person("Founder", "Tobie", "Morgan Hitchcock", true);

        List<Person> actual = surreal.update("person", expected);

        assertTrue(actual.size() > 1);
        actual.forEach(person -> {
            assertEquals(expected.getTitle(), person.getTitle());
        });
    }

}
