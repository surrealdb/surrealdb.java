package com.surrealdb.pojos;

import com.surrealdb.RecordId;
import com.surrealdb.Value;

public record RawRecord(RecordId id, Value raw, String name) {
}
