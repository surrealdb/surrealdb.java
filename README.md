# surrealdb.java

The official SurrealDB library for Java.

[![](https://img.shields.io/badge/status-beta-ff00bb.svg?style=flat-square)](https://github.com/surrealdb/surrealdb.java) [![](https://img.shields.io/badge/docs-view-44cc11.svg?style=flat-square)](https://surrealdb.com/docs/integration/libraries/java) [![](https://img.shields.io/badge/license-Apache_License_2.0-00bfff.svg?style=flat-square)](https://github.com/surrealdb/surrealdb.java)

### Features
- Sync & Async driver implementations available.
- Complex JSON serialization & deserialization to Java classes.
- Simple API (very similar to the Javascript driver, [see docs](https://surrealdb.com/docs/integration/libraries/java)).


### Installation

Gradle:
```groovy
ext {
	surrealdbVersion = "0.1.0"
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
	<version>0.1.0</version>
</dependency>
```


### Quick Start
```java
package org.example;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SurrealConnection connection = new SurrealWebSocketConnection("127.0.0.1", 8000);
        connection.connect(30); // timeout after 30 seconds

        SyncSurrealDriver driver = new SyncSurrealDriver(connection);

        driver.signIn("root", "root"); // username & password
        driver.use("test", "test"); // namespace & database

        Person tobie = driver.create("person", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));

        List<Person> people = driver.select("person", Person.class);

        System.out.println();
        System.out.println("Tobie = "+tobie);
        System.out.println();
        System.out.println("people = "+people);
        System.out.println();

        connection.disconnect();
        
        // for more docs, see https://surrealdb.com/docs/integration/libraries/java
    }
}

class Person {
    String id;
    String title;
    String firstName;
    String lastName;
    boolean marketing;

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
```

### Developing
You can build the project with `./gradlew build`.
Unit tests are run with `./gradlew test`.
Integration tests are run with `./gradlew integrationTest`.
Integration tests depend on having docker installed.

The following environment variables can be used to avoid relying on docker.

| ENV                 | Description                   |
|---------------------|-------------------------------|
| SURREALDB_JAVA_HOST | The host address of SurrealDB |
| SURREALDB_JAVA_PORT | The port address of SurrealDB |

### Planned Features
- A complete SDK With Repository pattern.
- JDBC interface (work in progress can be found in `src/main/java/com/surrealdb/jdbc`)
- Open an issue for feature requests


### Minimum Requirements
- Java 17

