# surrealdb-jni

A thread-safe native JAVA client for SurrealDB.

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

### Linux/Mac

```shell
cargo build
./gradlew -i test
```

### Windows

```shell
cargo build
./gradlew.bat -i test
```

### TODOs

- [x] Get a working build (java 8, cicd, releases)
- [ ] Get an API that resembles the JS/Rust drivers including type support: 50%
- [x] Include native rust binary for x86_64
- [x] Integrate native rust binary
- [x] Add additional architecture native binaries (including Android as a stretch goal)
- [ ] Unit and integration testing of streams, auth, params, and each query exposed via API: 50%
- [x] Error mapping