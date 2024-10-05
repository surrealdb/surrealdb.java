package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.List;
import java.util.Objects;

public class Person {

    public RecordId id;
    public String name;
    public List<String> tags;
    public long category;
    public boolean active;
    public List<Email> emails;

    public Person() {
    }

    public Person(String name, List<String> tags, long category, boolean active, List<Email> emails) {
        this.name = name;
        this.tags = tags;
        this.category = category;
        this.active = active;
        this.emails = emails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Person person = (Person) o;
        return category == person.category &&
            active == person.active &&
            Objects.equals(id, person.id) &&
            Objects.equals(name, person.name) &&
            Objects.deepEquals(tags, person.tags) &&
            Objects.deepEquals(emails, person.emails);
    }

    @Override
    public String toString() {
        return "id: " + id + ", name: " + name + ", tags: " + tags + ", category: " + category + ", active: " + active + ", emails: " + emails;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, tags, category, active, emails);
    }
}
