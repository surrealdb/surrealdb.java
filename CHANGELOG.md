# Changelog

## [Unreleased]

## [2.1.0] - 2026-06-04
- Upgrade the native layer to the `jni` crate 0.22.4; the Java/JNI ABI, public API, and JDK 8 minimum are unchanged [#166](https://github.com/surrealdb/surrealdb.java/pull/166).
- Upgrade to SurrealDB SDK 3.1.3 and bump the Rust toolchain to 1.95 [#165](https://github.com/surrealdb/surrealdb.java/pull/165).

## [2.0.3] - 2026-05-29
- Support Java `record` classes in `create` and `select` (JDK 16+ at runtime). Records are hydrated via their canonical constructor; POJO behaviour on JDK 8+ is unchanged [#156](https://github.com/surrealdb/surrealdb.java/pull/156).
- Add `Array.of()` and `Id.from(Object...)` factories for composite keys [#154](https://github.com/surrealdb/surrealdb.java/pull/154).
- Fix nullable `Boolean` and `Optional<T>` (de)serialization [#155](https://github.com/surrealdb/surrealdb.java/pull/155).
- Auto-load the native library on first use of any Native-backed POJO [#157](https://github.com/surrealdb/surrealdb.java/pull/157).
- Avoid spawning a new server session per health/version/export/import call [#161](https://github.com/surrealdb/surrealdb.java/pull/161).
- Document snapshot installs and auto-publish snapshots from `main` [#158](https://github.com/surrealdb/surrealdb.java/pull/158).
- Add LiveStream regression tests over WebSocket and `query()` variants [#159](https://github.com/surrealdb/surrealdb.java/pull/159).

## [2.0.2] - 2026-05-20
- Add Java query binding overloads and transaction bindings [#148](https://github.com/surrealdb/surrealdb.java/pull/148).
- Enforce spotless/cargo fmt on PRs and scope GITHUB_TOKEN permissions [#150](https://github.com/surrealdb/surrealdb.java/pull/150).
- Bump rustls-webpki from 0.103.9 to 0.103.13 [#146](https://github.com/surrealdb/surrealdb.java/pull/146).
- Bump rand from 0.8.5 to 0.8.6 [#145](https://github.com/surrealdb/surrealdb.java/pull/145).
- Bump lz4_flex from 0.12.0 to 0.12.1 [#144](https://github.com/surrealdb/surrealdb.java/pull/144).

## [2.0.1] - 2026-04-28
- Upgrade to SurrealDB SDK 3.0.5.
- Fix JVM crash when accessing array-backed RecordId keys [#141](https://github.com/surrealdb/surrealdb.java/pull/141).
- Surface selectLive() errors eagerly and improve LiveStream thread safety [#138](https://github.com/surrealdb/surrealdb.java/pull/138) [#139](https://github.com/surrealdb/surrealdb.java/pull/139).
- Fix Value.isRecordId() returning true for non-RecordId string values [#136](https://github.com/surrealdb/surrealdb.java/pull/136).
- Fix JNI exception class name casing, pin the Rust toolchain, and remove the deprecated `surrealdb_unstable` flag [#134](https://github.com/surrealdb/surrealdb.java/pull/134).
- Prepare the Java SDK for JDK 25 and harden CI/build tooling [#133](https://github.com/surrealdb/surrealdb.java/pull/133).

## [2.0.0] - 2026-03-17
- Upgrade to SurrealDB 3.0 - Java SDK v3.0.0 by @emmanuel-keller in [#118](https://github.com/surrealdb/surrealdb.java/pull/118)
- Support byte[] class fields for bytes type by @7Hazard in [#122](https://github.com/surrealdb/surrealdb.java/pull/122)
- 3.0 features by @kearfy in [#127](https://github.com/surrealdb/surrealdb.java/pull/127)
- Structured error handling by @kearfy in [#128](https://github.com/surrealdb/surrealdb.java/pull/128)
- exportSql & importSql naming by @kearfy in [#130](https://github.com/surrealdb/surrealdb.java/pull/130)
- Upgrade to SurrealDB SDK 3.0.4 in [#132](https://github.com/surrealdb/surrealdb.java/pull/132)

## [1.0.0] - 2025-02-XX

- Upgrade to SurrealDB SDK 2.2.1
- Enable remote HTTP connections [#110](https://github.com/surrealdb/surrealdb.java/pull/110)
- Implements queryBind [#106](https://github.com/surrealdb/surrealdb.java/pull/106) [#90](https://github.com/surrealdb/surrealdb.java/issues/90)
- Implements getBytes [#103](https://github.com/surrealdb/surrealdb.java/pull/103)

## [0.2.1] - 2025-01-14

- Upgrade to SurrealDB 2.1.4
- Fixed UnsatisfiedLinkError in public RecordId(String table, String id) [#93](https://github.com/surrealdb/surrealdb.java/pull/93)
- Response improvements [#94](https://github.com/surrealdb/surrealdb.java/pull/94)

## [0.2.0] - 2024-10-08

Native driver

## [0.1.0] - 2022-09-23

First GA release
