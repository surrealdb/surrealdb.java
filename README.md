# surrealdb

The official SurrealDB library for Java.

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

### Minimum Requirements

- Java 8

### Installation

Gradle:

```groovy
ext {
    surrealdbVersion = "0.2.0"
}

dependencies {
    implementation "com.surrealdb:surrealdb-driver:${surrealdbVersion}"
}
```

Maven:

```xml

<dependency>
    <groupId>com.surrealdb</groupId>
    <artifactId>surrealdb-driver</artifactId>
    <version>0.2.0</version>
</dependency>
```

### Quick Start

```java
package org.example;

import com.surrealdb.Surreal;

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
