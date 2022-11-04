# surrealdb.java

The official SurrealDB library for Java.

[![](https://img.shields.io/badge/status-beta-ff00bb.svg?style=flat-square)](https://github.com/surrealdb/surrealdb.java) [![](https://img.shields.io/badge/docs-view-44cc11.svg?style=flat-square)](https://surrealdb.com/docs/integration/libraries/java) [![](https://img.shields.io/badge/license-Apache_License_2.0-00bfff.svg?style=flat-square)](https://github.com/surrealdb/surrealdb.java)

### Features
- Sync & Async driver implementations available.
- Complex JSON serialization & deserialization to Java classes.
- Simple API for CRUD operations.
- Fluent API for building queries. (_Coming soon_)

### Supported SurrealDB features
- [x] Authentication
- [X] CRUD operations
- [X] Querying via SurrealQL
- [ ] Querying via Fluent API
- [ ] Live queries (when SurrealDB re-enables it)

### Installation
- For now, you can grab the JAR from the releases page [here](https://github.com/surrealdb/surrealdb.java/releases).
- Put it in `libs` folder.
- Add the JAR to you project:

Gradle:
```groovy
implementation files('libs/surrealdb-0.2.0.jar')
```

Maven:
```xml
<dependency>
    <groupId>com.surrealdb</groupId>
    <artifactId>driver</artifactId>
    <version>0.2.0</version>
    <scope>system</scope>
    <systemPath>${basedir}/libs/surrealdb-0.2.0.jar</systemPath>
</dependency>
```


### Quick Start
```java
package org.example;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealConnectionProtocol;
import com.surrealdb.driver.SyncSurrealDriver;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        SurrealConnectionSettings connectionSettings = SurrealConnectionSettings.Builder()
            .setUriFromComponents(SurrealConnectionProtocol.WEB_SOCKET, "localhost", 8000)
            .setAutoConnect(false)
            .build();
        
        SurrealConnection connection = SurrealConnection.create(connectionSettings);
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
        
        // for more docs, see https://surrealdb.com/docs/integration/libraries/java (coming soon)
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

### Planned Features
- A complete SDK with repository pattern.
- Fluent API for building queries.
- Open an issue for feature requests


### Minimum Requirements
- Java 8

