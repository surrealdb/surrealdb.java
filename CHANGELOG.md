# Changelog

## [2.1.2] - 2026-06-24
- Add full geometry-type support for reading and writing all seven types — Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, and GeometryCollection — via `Geometry` type-discrimination accessors (`getType()`, `isPolygon()`, …), readers returning `java.awt.geom.Point2D.Double` coordinates (x = longitude, y = latitude), and factory methods that serialize through `create`/`update` content and bound parameters. Also fixes an `UnsatisfiedLinkError` thrown when a `Geometry` was finalized (the native `deleteInstance` had no matching Rust symbol) [#183](https://github.com/surrealdb/surrealdb.java/pull/183).
- Add `Surreal.kill(String)` / `Surreal.kill(java.util.UUID)` to terminate a live query by id, and `LiveStream.getQueryId()` to read the live-query UUID immediately, before the first notification; `selectLive` now starts the subscription through the public `LIVE SELECT` query path so the id is available up front. `kill()` stops notifications but does not close a local `LiveStream` — use `LiveStream.close()` to release a blocked `next()` [#184](https://github.com/surrealdb/surrealdb.java/pull/184).
- Upgrade to SurrealDB SDK 3.1.5 [#185](https://github.com/surrealdb/surrealdb.java/pull/185).
- Run CI against SurrealDB server v3.1.5, matching the embedded SDK; previously v3.1.3 [#185](https://github.com/surrealdb/surrealdb.java/pull/185).
- Align the declared `tokio` (1.52.1) and `rust_decimal` (1.41.0) minimums in `Cargo.toml` with SurrealDB 3.1.5's workspace; resolved lockfile versions are unchanged [#185](https://github.com/surrealdb/surrealdb.java/pull/185).

## [2.1.1] - 2026-06-10
- Support more Java datetime types in value conversion: `Instant`, `OffsetDateTime`, `LocalDateTime` (interpreted as UTC), `java.util.Date`, and the `java.sql` date types, in query bindings, `Array.of()`/`Id.from()`, and POJO/record round trips [#172](https://github.com/surrealdb/surrealdb.java/pull/172).
- Add `@SurrealName` to map Java fields and record components to explicit SurrealDB object keys, honored in both serialization and deserialization [#173](https://github.com/surrealdb/surrealdb.java/pull/173).
- Deserialization no longer assigns database keys to `static` or `transient` fields; such keys are now ignored, mirroring the write path [#173](https://github.com/surrealdb/surrealdb.java/pull/173).
- Upgrade to SurrealDB SDK 3.1.4 [#177](https://github.com/surrealdb/surrealdb.java/pull/177).
- Refresh `aws-lc-rs`/`aws-lc-sys` in the lockfile, clearing two high security advisories (GHSA-9f94-5g5w-gf6r, GHSA-394x-vwmw-crm3) [#177](https://github.com/surrealdb/surrealdb.java/pull/177).
- Run CI against SurrealDB server v3.1.3, the first public 3.1.x server release [#177](https://github.com/surrealdb/surrealdb.java/pull/177).
- Run the Java-record test suite in CI, and emit per-test events at INFO level so they appear in CI logs [#178](https://github.com/surrealdb/surrealdb.java/pull/178).
- Serialize inherited POJO fields, which were previously silently dropped on write: both conversion paths now walk the user-defined class hierarchy with the same semantics. The walk stops at the first JDK class (JDK internals such as `java.lang.Enum.name` are never serialized), and per JLS 8.3 a `static` or `transient` subclass field hides its same-named superclass field on both paths [#179](https://github.com/surrealdb/surrealdb.java/pull/179).
- Cache per-class field metadata in the value converters; conversion no longer re-resolves fields, annotations, and record components for every object [#180](https://github.com/surrealdb/surrealdb.java/pull/180).

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
