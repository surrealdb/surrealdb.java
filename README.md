<br>

<p align="center">
    <img width=120 src="https://raw.githubusercontent.com/surrealdb/icons/main/surreal.svg" />
    &nbsp;
    <img width=120 src="https://raw.githubusercontent.com/surrealdb/icons/main/java.svg" />
</p>

<h3 align="center">The official SurrealDB SDK for Java</h3>

<br>

<p align="center">
    <a href="https://github.com/surrealdb/surrealdb.java"><img src="https://img.shields.io/badge/status-stable-ff00bb.svg?style=flat-square"></a>
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
- Mutable POJOs (Java 8+) and immutable `record` classes (JDK 16+) for `create` / `select`.
- All geometry types (Point, LineString, Polygon, their multi-variants, and GeometryCollection) for reading and writing.
- Sized number kinds (`i8` ... `u64`, `f16`, `f32`) for reading and writing against SurrealDB 4.0.
- Live queries: subscribe to table changes with `selectLive` and terminate them by id with `kill`.
- Supported on JAVA JDK 8, 11, 17, 21, 25.
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

### Stable release

Released to [Maven Central](https://central.sonatype.com/artifact/com.surrealdb/surrealdb).

Gradle:

```groovy
ext {
    surrealdbVersion = "2.2.0"
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
    <version>2.2.0</version>
</dependency>
```

### Snapshot release

Published to the [Maven Central snapshots repository](https://central.sonatype.com/repository/maven-snapshots/) on every push to `main`. Use it to try out unreleased changes.

Gradle:

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        mavenContent { snapshotsOnly() }
    }
}

ext {
    surrealdbVersion = "3.0.0-SNAPSHOT"
}

dependencies {
    implementation "com.surrealdb:surrealdb:${surrealdbVersion}"
}
```

Maven:

```xml
<repositories>
    <repository>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <releases><enabled>false</enabled></releases>
        <snapshots><enabled>true</enabled></snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>com.surrealdb</groupId>
    <artifactId>surrealdb</artifactId>
    <version>3.0.0-SNAPSHOT</version>
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

### Geometry

All GeoJSON-style geometry types are supported — `Point`, `LineString`, `Polygon`,
`MultiPoint`, `MultiLineString`, `MultiPolygon`, and `GeometryCollection` — for both
reading and writing. Coordinates use `java.awt.geom.Point2D.Double`, where `x` is the
longitude and `y` is the latitude.

```java
import com.surrealdb.Geometry;
import com.surrealdb.Surreal;
import com.surrealdb.Value;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

try (final Surreal driver = new Surreal()) {
    driver.connect("memory").useNs("test").useDb("test");

    // Build a geometry in Java and store it via a bound parameter
    Geometry route = Geometry.lineString(Arrays.asList(
            new Point2D.Double(-0.118, 51.51),
            new Point2D.Double(-0.121, 51.50)));
    Map<String, Object> params = new HashMap<>();
    params.put("route", route);
    driver.query("UPSERT path:1 SET route = $route", params);

    // Read it back
    Value value = driver.query("SELECT VALUE route FROM path:1").take(0).getArray().get(0);
    Geometry geom = value.getGeometry();
    System.out.println(geom.getType());        // LineString
    System.out.println(geom.getLineString());  // [Point2D.Double[...], Point2D.Double[...]]
}
```

### Sized numbers (SurrealDB 4.0)

SurrealDB 4.0 adds the sized number kinds `i8`, `u8`, `i16`, `u16`, `i32`, `u32`, `u64`, `f16`
and `f32` next to `int` (`i64`), `float` (`f64`) and `decimal`.

Reading: `isLong()`/`getLong()` work for every integer kind whose value fits a `long`, and
`isDouble()`/`getDouble()` work for every floating-point kind. A `u64` above `Long.MAX_VALUE`
is never reported as a long (it would wrap); use `isBigInteger()`/`getBigInteger()`.
`getNumberType()` returns the exact kind as a `NumberType`.

Writing: Java `byte`, `short`, `int`, `long`, `float` and `double` still map to `int` and `float`,
so existing schemas and 3.x servers behave as before. Opt in to a sized kind explicitly with
`ValueMut.createI8`, `createU8`, `createI16`, `createU16`, `createI32`, `createU32`, `createU64`,
`createF16` and `createF32`.

Results keep their kind on the embedded engine and over a remote connection to a 4.0 server. Values
you send are different: the embedded engine keeps the kind of a bound `ValueMut`, but a remote
connection widens it (sized integers become `int`, floats become `float`, a `u64` above
`Long.MAX_VALUE` becomes a `decimal`), so over the network a `TYPE u8` (etc.) schema field is what
makes the stored kind sized.

```java
try (final Surreal driver = new Surreal()) {
    driver.connect("memory").useNs("test").useDb("test");

    Map<String, Object> params = new HashMap<>();
    params.put("small", ValueMut.createU8(200));
    params.put("big", ValueMut.createU64(new BigInteger("18446744073709551615")));
    driver.query("CREATE sample:1 SET small = $small, big = $big", params);

    Value row = driver.query("SELECT * FROM sample:1").take(0).getArray().get(0);
    Value big = row.getObject().get("big");
    System.out.println(big.getNumberType());  // U64
    System.out.println(big.isLong());         // false
    System.out.println(big.getBigInteger());  // 18446744073709551615
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
