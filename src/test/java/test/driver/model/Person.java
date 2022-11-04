package test.driver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Khalid Alharisi
 */
@Data
public class Person {

    private String id;
    private String title;
    private Name name;
    private boolean marketing;

    public Person(String title, String firstName, String lastName, boolean marketing) {
        this.title = title;
        this.name = new Name(firstName, lastName);
        this.marketing = marketing;
    }

    @Data
    @AllArgsConstructor
    public static class Name {
        private String first;
        private String last;
    }

}
