package com.surrealdb.pojos;

import com.surrealdb.Thing;

import java.util.List;
import java.util.Optional;

public class Person {
    public Optional<Thing> id;
    public String name;
    public List<String> tags;
    public long category;
    public boolean active;
    public List<Email> emails;

    public Person() {
    }

    public Person(Optional<Thing> id, String name, List<String> tags, long category, boolean active, List<Email> emails) {
        this.id = id;
        this.name = name;
        this.tags = tags;
        this.category = category;
        this.active = active;
        this.emails = emails;
    }
}