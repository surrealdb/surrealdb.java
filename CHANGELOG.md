# Changelog

## [Unreleased] - 3.0.0
- Build on SurrealDB 4.0.0 (the `surrealdb` crate from crates.io). The embedded engine is now SurrealDB 4.0 and the SDK still connects to 3.x and 4.x servers (the crate accepts servers from 3.0.0-alpha.1 up to, but excluding, 5.0.0).
- Breaking: RocksDB (`rocksdb://`) is no longer available, because the 4.0 crate has no RocksDB backend. Use `surrealkv://` for a persistent embedded database. The `kv-rocksdb` and `portable` Cargo features are removed, and the 32-bit and Android native libraries are built with the default features like every other target.
- Read sized numbers: `Value.isLong()`/`getLong()` work for every integer kind that fits a `long`, and `isDouble()`/`getDouble()` for `f16`, `f32` and `float`. A `u64` above `Long.MAX_VALUE` is never reported as a long (`getLong()` throws); use the new `isBigInteger()`/`getBigInteger()`. The new `Value.isNumber()` and `Value.getNumberType()` (a `NumberType` enum) expose the exact kind. POJO and record conversion fills `byte`, `short`, `int`, `long`, `float`, `double` and `BigInteger` fields from any numeric kind.
- Read the new SurrealDB 4.0 `bigdecimal` kind: `Value.isBigDecimal()`/`getBigDecimal()` accept it like `decimal`, and `NumberType.BIGDECIMAL` identifies it.
- Write sized numbers with the explicit `ValueMut.createI8/U8/I16/U16/I32/U32/U64/F16/F32` factories, which validate unsigned ranges. Existing Java types still map to `int` and `float`.
- Run CI against the SurrealDB 4.0.0 server (the `surrealdb/surrealdb` Docker Hub image on Linux, the release tarball from download.surrealdb.com on macOS) and against the latest 3.x patch of each minor (v3.1.6, v3.2.6, v3.3.5) on JDK 17.

## [2.2.0] - 2026-11-03
- Support RocksDB (`rocksdb://`, enabled by default through the `kv-rocksdb` Cargo feature; 32-bit and Android targets must build with `--no-default-features --features portable`) and gRPC (`grpc://`, enabled by default) [#212](https://github.com/surrealdb/surrealdb.java/pull/212).
- Add `Surreal.set(String, Object)` and `Surreal.unset(String)` to define and remove a session variable for later queries, and `Surreal.info()` / `Surreal.info(Class<T>)` to read the signed-in record user's record (`$auth`), which is empty for root and anonymous sessions [#208](https://github.com/surrealdb/surrealdb.java/pull/208).
- Add `Surreal.refresh(Token)` to exchange a refresh token for a new token pair and re-authenticate the session, and `Surreal.revoke(Token)` to revoke a refresh token. Both throw `IllegalArgumentException` when the token has no refresh part, because only record access methods defined `WITH REFRESH` issue one [#208](https://github.com/surrealdb/surrealdb.java/pull/208).
- Add a `RecordIdRange(table, start, startInclusive, end, endInclusive)` constructor for ranges with exclusive bounds, and `Value.isRangeStartInclusive()` / `Value.isRangeEndInclusive()` to read the bound kinds. Ranges were always inclusive at both ends before, and the existing three-argument constructor still includes both [#209](https://github.com/surrealdb/surrealdb.java/pull/209).
- Support the SurrealQL set type: read it with `Value.isSet()` / `Value.getSet()`, write it with `ValueMut.createSet(Collection<?>)`, and convert set and array values into `Set` or `List` POJO fields. `java.util.Set` bindings and fields are still sent as arrays [#209](https://github.com/surrealdb/surrealdb.java/pull/209).
- Add `Surreal.selectLive(String table, boolean diff)`, which runs `LIVE SELECT DIFF` so each notification carries JSON Patch operations instead of the record. Diff mode is refused with a `SurrealException` on SurrealDB before 3.3.0, because earlier versions can leak fields hidden by field-level `SELECT` permissions (GHSA-pm6c-q8rh-92m8) [#210](https://github.com/surrealdb/surrealdb.java/pull/210).
- Add `UpType.REPLACE` for `UPDATE … REPLACE` and `UPSERT … REPLACE` with every update and upsert overload [#211](https://github.com/surrealdb/surrealdb.java/pull/211).
- Security: fix SurrealQL injection in the CRUD helpers and `run()`. Table names, record ids and contents are now bound as variables instead of being spliced into the query text, and `run()` accepts only identifier segments joined by `::` with an optional numeric version (such as `ml::model<1.0.0>`), rejecting anything else before it is sent. Previously a table name such as `victim; REMOVE TABLE victim` ran the second statement [#203](https://github.com/surrealdb/surrealdb.java/pull/203).
- Behaviour change: a `String` target passed to `select`, `update`, `upsert`, `delete` and the other table-taking methods is now always a table name, never parsed as a record id. `select("person:tobie")` used to work by accident and now addresses a table literally named `person:tobie`; use a `RecordId` for single records [#203](https://github.com/surrealdb/surrealdb.java/pull/203).
- Fix `update(String[], …)`, `updateSync(String[], …)`, `upsert(String[], …)` and `upsertSync(String[], …)`, which threw `UnsatisfiedLinkError` because the native symbol was missing [#203](https://github.com/surrealdb/surrealdb.java/pull/203).
- Fix native memory safety: native objects are now freed exactly once, by a cleaner instead of `finalize()`, which removes double frees, use-after-free on temporaries passed to native calls, and leaks from native types that had no destructor. Calling `close()` twice is safe, and using a closed instance throws `NullPointerException` [#205](https://github.com/surrealdb/surrealdb.java/pull/205).
- Make `Response`, `Value`, `Array`, `Object`, `Entry`, `ValueIterator`, `EntryIterator` and `Transaction` implement `AutoCloseable`, so results can be freed right away with try-with-resources instead of waiting for the garbage collector. `Transaction.close()` cancels a transaction that was neither committed nor cancelled. The SDK also closes the wrappers it creates internally when converting results to POJOs and records, which reduces memory growth in read-heavy loops. An instance that is never closed is still freed once it is garbage collected [#205](https://github.com/surrealdb/surrealdb.java/pull/205).
- Fix durations losing precision: `Value.getDuration()` and bound `java.time.Duration` values now keep full nanosecond precision instead of being truncated to milliseconds. A negative `java.time.Duration` now throws `SurrealException` instead of wrapping into a huge value, since SurrealDB durations are unsigned, and a stored duration too large for `java.time.Duration` throws instead of wrapping [#202](https://github.com/surrealdb/surrealdb.java/pull/202).
- Fix the namespace and database of a session created by `newSession()`: it now reports and uses the values it inherited from its parent instead of `null`, so `getNamespace()`, `getDatabase()` and `signin`/`signup` with a `RecordCredential` that omits them work on the new session [#204](https://github.com/surrealdb/surrealdb.java/pull/204).
- Fix `Surreal.kill(...)` silently succeeding for an unknown, already-killed or foreign live query: it now throws a `QueryException` ("Cannot execute KILL statement using id: …") [#206](https://github.com/surrealdb/surrealdb.java/pull/206).
- Upgrade to SurrealDB SDK 3.3.5, and run CI against SurrealDB server v3.3.5 (previously v3.1.5). SurrealDB calls now run on Tokio worker threads with 10 MiB stacks, as SurrealDB recommends, so a Java thread with a small stack (1 MiB by default on Linux) no longer overflows [#212](https://github.com/surrealdb/surrealdb.java/pull/212).
- Run CI against the latest 3.x release on every JDK and against the latest patch of each earlier 3.x minor (v3.1.6 and v3.2.6) on JDK 17, and add server-backed tests over WebSocket and HTTP, with the test server taken from `SURREALDB_URL` (default `ws://localhost:8000`) [#207](https://github.com/surrealdb/surrealdb.java/pull/207).
- Strip symbols from the release native libraries to reduce the size of the jar [#212](https://github.com/surrealdb/surrealdb.java/pull/212).
- Refresh all dependencies in `Cargo.lock` to their latest compatible versions, including `quinn-proto` 0.11.19, `xxhash-rust` 0.8.19, `rustls` 0.23.46, `aws-lc-rs` 1.19.0 and `tokio` 1.54.1 [#212](https://github.com/surrealdb/surrealdb.java/pull/212).

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
