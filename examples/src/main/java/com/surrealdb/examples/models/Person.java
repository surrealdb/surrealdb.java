package com.surrealdb.examples.models;

import com.surrealdb.types.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Person {

    private Id id; // This will be automatically assigned by SurrealDB when the object is saved
    private String title;
    private Name name;
    private boolean marketing;

    public void setName(String firstName, String lastName) {
        this.name = new Name(firstName, lastName);
    }

    @Data
    @AllArgsConstructor
    public static class Name {

        private String firstName;
        private String lastName;

    }
}
