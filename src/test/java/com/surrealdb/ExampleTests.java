package com.surrealdb;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

public class ExampleTests {

    @Test
    void example() {
        try (final Surreal driver = new Surreal()) {
            // Connect to the instance
            driver.connect("memory");
            // namespace & database
            driver.useNs("test").useDb("test");
            // Create a person
            Person person = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
            // Insert a record
            List<Person> tobie = driver.create(Person.class, "person", person);
            // Read records
            Iterator<Person> people = driver.select(Person.class, "person");
            // Print them out
            System.out.println("Tobie = " + tobie);
            System.out.println("people = " + people.next());
        }
    }

    static class Person {
        String id;
        String title;
        String firstName;
        String lastName;
        boolean marketing;

        //  A default constructor is required
        public Person() {
        }

        public Person(String title, String firstName, String lastName, boolean marketing) {
            this.title = title;
            this.firstName = firstName;
            this.lastName = lastName;
            this.marketing = marketing;
        }

        @Override
        public String toString() {
            return "Person{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", marketing=" + marketing +
                '}';
        }
    }
}

