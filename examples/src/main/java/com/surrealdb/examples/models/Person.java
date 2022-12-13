package com.surrealdb.examples.models;

import com.surrealdb.types.SurrealRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Person extends SurrealRecord {

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
