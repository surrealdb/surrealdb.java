package meta.model;

import com.surrealdb.types.Id;
import com.surrealdb.types.SurrealRecord;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Khalid Alharisi
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class Person extends SurrealRecord {

    public static final @NotNull Person TOBIE = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
    public static final @NotNull Person JAIME = new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true);

    String title;
    Name name;
    boolean marketing;

    public Person(@Nullable Id id, String title, Name name, boolean marketing) {
        super(id);
        this.title = title;
        this.name = name;
        this.marketing = marketing;
    }

    public Person(String title, String firstName, String lastName, boolean marketing) {
        this.title = title;
        this.name = new Name(firstName, lastName);
        this.marketing = marketing;
    }

    public Person(String title, Name name, boolean marketing) {
        this.title = title;
        this.name = name;
        this.marketing = marketing;
    }

    @Value
    public static class Name {

        String first;
        String last;

    }
}
