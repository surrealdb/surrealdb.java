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
public class QuickStart {

    public static void main(String[] args) {
        // Create a connection with the minimal amount of configuration
        SurrealConnection connection = SurrealConnection.create(SurrealConnectionProtocol.WEB_SOCKET, "localhost", 8000);
        // If the connection is not established within 15 seconds, an exception will be thrown.
        connection.connect(15);

        // Create a synchronous driver without any driver settings
        SurrealDriver driver = SurrealDriver.create(connection);

        // Sign in with the user 'root' and the password 'root'
        SurrealAuthCredentials credentials = SurrealRootCredentials.from("root", "root");

        // Sign in with the newly created credentials
        driver.signIn(credentials);

        // Use the namespace 'examples' and the database 'quickstart'
        driver.use("examples", "quickstart");

        // Create a reference to the "person" table
        // note: Creating a table reference has no effect on the database.
        //       Table references are just wrappers around the table name
        //       and type of object that will be stored within the table.
        SurrealTable<Person> personTable = SurrealTable.of("person", Person.class);

        // Create a new person
        Person tobie = new Person();
        tobie.setTitle("Founder & CEO");
        tobie.setName("Tobie", "Morgan Hitchcock");
        tobie.setMarketing(true);

        try {
            // Save the person to the database
            System.out.println("Saving person to database...");
            driver.createRecord(personTable, "tobie", tobie);
        } catch (SurrealRecordAlreadyExistsException e) {
            // This exception will be thrown if the record already exists
            // in the database. In this case, we will just update the record
            // instead of creating a new one.

            // Try running the program twice to see this behavior in action

            System.out.println("Record already exists, updating instead...");
            driver.updateRecord(personTable, "tobie", tobie);
        }

        // Retrieve the person from the database
        // note: Retrieving a record from the DB returns an Optional. This is to
        //       make it almost impossible to throw a null pointer exception.
        Optional<Person> retrievedTobie = driver.retrieveRecord(personTable, "tobie");

        // Print the retrieved person
        retrievedTobie.ifPresentOrElse(
            person -> System.out.println("Retrieved person: " + person),
            () -> System.err.println("Failed to retrieve person")
        );

        // Disconnect from the database. This is required in order to exit since
        // the connection is running in a separate thread.
        connection.disconnect();
    }
}

@Data
public class Person {

    private String id; // This will be automatically assigned by SurrealDB when the object is saved
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
```

### Planned Features
- A complete SDK with repository pattern.
- Fluent API for building queries.
- Support for all SurrealDB features.
- Open an issue for feature requests


### Minimum Requirements
- Java 8

