# SurrealDB Java Driver
SurrealDB driver for Java.

![](https://img.shields.io/badge/status-beta-ff00bb.svg?style=flat-square) ![](https://img.shields.io/badge/license-Apache_License_2.0-00bfff.svg?style=flat-square)

### Features
- Sync & Async driver implementations available.
- Automatic JSON serialization & deserialization to Java classes.
- Simple API (very similar to the Javascript driver, [see docs](https://surrealdb.com/docs/integration/libraries/nodejs#:~:text=node%20app.js-,Library%20methods,-The%20JavaScript%20library)).


### Installation
For now, you can grab the JAR from the releases page [here](https://github.com/coder966/surrealdb.java/releases).

#### To add the JAR to you project (Gradle):
`implementation files('libs/surrealdb-0.1.0.jar')`

#### To add the JAR to you project (Maven):
```xml
<dependency>
    <groupId>com.surrealdb.java</groupId>
    <artifactId>driver</artifactId>
    <version>0.1.0</version>
    <scope>system</scope>
    <systemPath>${basedir}/libs/surrealdb-0.1.0.jar</systemPath>
</dependency>
```


### Quick Start
```java
SurrealConnection connection = new SurrealWebSocketConnection("127.0.0.1", 8000);
connection.connect(30); // timeout after 30 seconds

SyncSurrealDriver driver = new SyncSurrealDriver(connection);

driver.signIn("root", "root"); // username & password
driver.use("test", "test"); // namespace & database

Person tobie = driver.create("person", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));

List<Person> people = driver.select("person", Person.class);

// for more docs, see https://surrealdb.com/docs/integration/libraries/nodejs
```

### Planned Features
- A complete SDK With Repository pattern.
- You tell me :)


### Minimum Requirements
- Java 8

