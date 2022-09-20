import com.surrealdb.java.Surreal;
import lombok.extern.slf4j.Slf4j;
import model.Person;
import org.junit.jupiter.api.*;

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
        Person person = new Person("Founder and CEO", "Tobie", "Morgan Hitchcock", true);
        person = surreal.create("person", person);
        log.info("new person {}", person);
    }

    @Test
    @Order(5)
    public void testCreateWithId() {
        Person person = new Person("Founder and CEO", "Tobie", "Morgan Hitchcock", true);
            person = surreal.create("person:"+personId, person);
        log.info("new person {}", person);
    }

}
