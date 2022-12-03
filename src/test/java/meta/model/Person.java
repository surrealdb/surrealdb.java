package meta.model;

import com.google.common.base.Objects;
import com.surrealdb.types.Id;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * @author Khalid Alharisi
 */
@Value
public class Person {

    public static final @NotNull Person TOBIE = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
    public static final @NotNull Person JAIME = new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true);

    Id id;
    String title;
    Name name;
    boolean marketing;

    public Person(String title, String firstName, String lastName, boolean marketing) {
        id = null;

        this.title = title;
        this.name = new Name(firstName, lastName);
        this.marketing = marketing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return marketing == person.marketing && Objects.equal(title, person.title) && Objects.equal(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title, name, marketing);
    }

    @Value
    public static class Name {

        String first;
        String last;

    }
}
