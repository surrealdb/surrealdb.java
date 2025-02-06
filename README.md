<br>

<p align="center">
    <img width=120 src="https://raw.githubusercontent.com/surrealdb/icons/main/surreal.svg" />
    &nbsp;
    <img width=120 src="https://raw.githubusercontent.com/surrealdb/icons/main/java.svg" />
</p>

<h3 align="center">The official SurrealDB SDK for Java</h3>

<br>

<p align="center">
    <a href="https://github.com/surrealdb/surrealdb.java"><img src="https://img.shields.io/badge/status-beta-ff00bb.svg?style=flat-square"></a>
    &nbsp;
    <a href="https://surrealdb.com/docs/integration/libraries/java"><img src="https://img.shields.io/badge/docs-view-44cc11.svg?style=flat-square"></a>
    &nbsp;
    <a href="https://pkg.go.dev/github.com/surrealdb/surrealdb.java"><img src="https://img.shields.io/maven-central/v/com.surrealdb/surrealdb?style=flat-square&label=maven&color=f58219"></a>
    &nbsp;
    <a href="https://github.com/surrealdb/surrealdb.java"><img src="https://img.shields.io/badge/java-8+-f58219.svg?style=flat-square"></a>
</p>

<p align="center">
    <a href="https://surrealdb.com/discord"><img src="https://img.shields.io/discord/902568124350599239?label=discord&style=flat-square&color=5a66f6"></a>
    &nbsp;
    <a href="https://twitter.com/surrealdb"><img src="https://img.shields.io/badge/twitter-follow_us-1d9bf0.svg?style=flat-square"></a>
    &nbsp;
    <a href="https://www.linkedin.com/company/surrealdb/"><img src="https://img.shields.io/badge/linkedin-connect_with_us-0a66c2.svg?style=flat-square"></a>
    &nbsp;
    <a href="https://www.youtube.com/channel/UCjf2teVEuYVvvVC-gFZNq6w"><img src="https://img.shields.io/badge/youtube-subscribe-fc1c1c.svg?style=flat-square"></a>
</p>

# surrealdb.java

The official SurrealDB SDK for Java.

## Documentation

View the SDK documentation [here](https://surrealdb.com/docs/integration/libraries/java).

### Features

- A thread-safe native JAVA client for SurrealDB.
- Simple API: [see docs](https://surrealdb.com/docs/integration/libraries/java).
- Support of 'memory' (embedded SurrealDB).
- Support of remote connection to SurrealDB.
- Supported on JAVA JDK 8, 11, 17, 21, 22.
- Supported architectures:
    - Linux (ARM) aarch64
    - Linux (INTEL) x86_64
    - Windows (INTEL) x86_64
    - MacOS (ARM) aarch64
    - MacOS (INTEL) x86_64
    - Android (Linux ARM) aarch64
    - Android (Linux INTEL) x86_64
- Zero dependencies

## Minimum requirements

- Java 8

## How to install

Gradle:

```groovy
ext {
    surrealdbVersion = "0.2.2-SNAPSHOT"
}

dependencies {
    implementation "com.surrealdb:surrealdb:${surrealdbVersion}"
}
```

Maven:

```xml

<dependency>
    <groupId>com.surrealdb</groupId>
    <artifactId>surrealdb</artifactId>
    <version>0.2.2-SNAPSHOT</version>
</dependency>
```

## Getting started

```java
package org.example;

import com.surrealdb.Surreal;
import com.surrealdb.RecordId;

import java.util.Iterator;

public class Example {
    public static void main(String[] args) {
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
        RecordId id;
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
```

### Reports

- [Javadoc](https://surrealdb.github.io/surrealdb.java/javadoc/)
- [Test summary](https://surrealdb.github.io/surrealdb.java/tests/test/)
- [Coverage report](https://surrealdb.github.io/surrealdb.java/jacoco/test/html/index.html)

### Developing

On Linux/Mac:

```shell
cargo build
./gradlew -i test
```

On Windows:

```shell
cargo build
./gradlew.bat -i test
```

### Planned Features

- All Geometry types (actually only points)
- Ranges
- Future
- Live queries
- Open an issue for feature requests
