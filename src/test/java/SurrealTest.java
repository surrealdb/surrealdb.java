import com.surrealdb.java.Surreal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SurrealTest {

    private Surreal surreal;

    @BeforeAll
    public void init(){
        surreal = new Surreal("172.18.0.2", 8000);
    }

}