package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.List;
import java.util.Optional;

public record PersonRecord(RecordId id, String name, List<String> tags, long category, boolean active,
		List<EmailRecord> emails, Optional<String> nickname) {
}
