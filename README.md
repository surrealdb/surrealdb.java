# surrealdb-jni

A native JAVA client for SurrealDB.

- Supported JAVA version: JAVA 8, 11, 17, 21, 22.
- Supported architectures:
    - Linux (ARM) aarch64
    - Linux (INTEL) x86_64
    - Windows (INTEL) x86_64
    - MacOS (ARM) aarch64
    - MacOS (INTEL) x86_64
    - Android (Linux ARM) aarch64
    - Android (Linux INTEL) x86_64

### Reports

[Test report, Code coverage, Javadoc](https://emmanuel-keller.github.io/surrealdb-jni/)

### Unix

```shell
cargo build
./gradlew -i test
```

### Windows

```shell
cargo build
./gradlew.bat -i test
```