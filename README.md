# surrealdb-jni

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