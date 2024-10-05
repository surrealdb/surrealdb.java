package com.surrealdb;

import com.surrealdb.pojos.Email;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Person;
import com.surrealdb.pojos.Review;

import java.util.Arrays;
import java.util.Collections;

public class Helpers {

    public static final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
    public static final Person jaime = new Person("Jaime", Collections.singletonList("COO"), 2, true, Collections.singletonList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
    public static final Person emmanuel = new Person("Emmanuel", Collections.emptyList(), 3, true, Collections.singletonList(new Email("emmanuel@example.com", new Name("Emmanuel", "Baz"))));
    public static final Review review = new Review(5, "So cool!");

}
