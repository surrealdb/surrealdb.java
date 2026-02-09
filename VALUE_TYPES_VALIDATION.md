# Value/Data Types Validation – SurrealDB Java Client

Validation against SurrealDB’s value model ([Values](https://surrealdb.com/docs/surrealql/datamodel/values)) and your requested list.

---

## ✅ Present and supported

| Type | In Value.java | Notes |
|------|----------------|--------|
| **Null** | `isNull()` | Separate from none. |
| **None** | `isNone()` | Separate from null. |
| **Boolean** | `isBoolean()`, `getBoolean()` | |
| **Int** | `isLong()`, `getLong()` | 64-bit integer. |
| **Float** | `isDouble()`, `getDouble()` | 64-bit float. |
| **Decimal** | `isBigDecimal()`, `getBigDecimal()` | 128-bit decimal (Java `BigDecimal`). |
| **String** | `isString()`, `getString()` | |
| **Record ID** | `isRecordId()`, `getRecordId()` → `RecordId` | Maps to SurrealDB record IDs. |
| **Object** | `isObject()`, `getObject()` | |
| **Arrays** | `isArray()`, `getArray()` | |
| **Durations** | `isDuration()`, `getDuration()` | Java `Duration` (millis in native). |
| **Geometries** | `isGeometry()`, `getGeometry()` → `Geometry` | |
| **Datetime** | `isDateTime()`, `getDateTime()` | Java `ZonedDateTime` (UTC). |
| **Bytes** | `isBytes()`, `getBytes()` | Binary data. |
| **UUID** | `isUuid()`, `getUuid()` | Standalone UUID value. |

---

## ⚠️ Record ID – partial

- **Supported for creation:** `RecordId(table, long)`, `RecordId(table, String)`, `RecordId(table, UUID)` only.
- **Supported for reading:** All key types. `value.getRecordId().getId()` returns `Id`, which has:
  - `isLong()` / `getLong()`
  - `isString()` / `getString()`
  - `isUuid()` / `getUuid()`
  - `isArray()` / `getArray()`
  - `isObject()` / `getObject()`
- **Supported for creation:** `RecordId(table, long)`, `RecordId(table, String)`, `RecordId(table, UUID)`, `RecordId(table, Array)`, `RecordId(table, Object)`.

---

## ❌ Not present as first-class value types

These exist in SurrealDB’s value model but are **not** exposed on `Value` / `ValueMut` in this client:

| SurrealDB type | Status in Java client |
|----------------|------------------------|
| **File (file pointers)** | Supported. `Value.isFile()`, `Value.getFile()` → `FileRef` (getBucket, getKey). `ValueMut.createFile(bucket, key)`. |
| **Sets** | Not supported. Sets are distinct from arrays in SurrealDB; the client only has array support. Collections are mapped to arrays in `ValueBuilder`. |
| **Ranges** | Supported (read). `Value.isRange()`, `Value.getRangeStart()` / `Value.getRangeEnd()` → `Optional<Value>`. Creation not yet exposed. |
| **Record ID ranges** | Supported via `RecordIdRange` (table + optional start/end `Id`). `Surreal.select(RecordIdRange)`, `update(RecordIdRange, ...)`, `delete(RecordIdRange)`, `upsert(RecordIdRange, ...)`. Range-based IDs are not supported for single-record Id serialization in `valuemut.rs`. |
| **Table** | Supported. `Value.isTable()`, `Value.getTable()` → table name string. `ValueMut.createTable(name)`. |
| **Regex** | Not supported. Regex semantics differ by language; not transferrable across the boundary (same as other SDKs). Encountering a regex value can result in a serialization error. |

So currently **not** supported as first-class value types:

- File pointers  
- Geometries ✅ (supported)  
- Decimal ✅ (supported)  
- Float ✅ (supported)  
- Int ✅ (supported)  
- String ✅ (supported)  
- Boolean ✅ (supported)  
- Null / None ✅ (supported as separate types)  
- Record ID ✅ (supported; name “Thing”; array/object keys via RecordId(table, Array) / RecordId(table, Object))  
- Record ID ranges ✅ (`RecordIdRange`, select/update/delete/upsert)  
- Object ✅ (supported)  
- Arrays ✅ (supported)  
- Sets ❌ (only arrays; sets are not a separate value)  
- Durations ✅ (supported)  
- Ranges ✅ (read: getRangeStart/getRangeEnd)  
- Table ✅ (isTable/getTable, createTable)  
- Regex ❌ (not transferrable; serialization error if encountered)  
- File ✅ (isFile/getFile→FileRef, createFile)  
- Bytes ✅ (supported)  
- Datetime ✅ (supported)  
- UUID ✅ (supported)  

---

## Other SurrealDB value types (from docs)

- **Closures / Futures:** Typically not exposed as generic “values” in SDKs; no support in this client.
- **Formatters / Idioms / Literals:** Language/syntax concepts, not usually separate runtime value types in the client.

---

## Summary

- **Fully supported:** null, none, boolean, int, float, decimal, string, record ID, object, array, duration, geometry, datetime, bytes, UUID.
- **Record ID:** Read and create paths support string, int, UUID, array, and object keys.
- **Not supported as value types:** sets (as distinct from arrays), regex (not transferrable; serialization error if encountered). File, range (read), and table are supported. Record ID ranges are supported for queries via `RecordIdRange`.

If you want, I can suggest concrete API changes (e.g. `RecordId(table, Id)` or new `Value`/`ValueMut` methods) for the missing types or record ID creation.
