# Changelog

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
