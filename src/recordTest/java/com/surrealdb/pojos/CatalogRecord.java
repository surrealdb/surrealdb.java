package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.List;
import java.util.Map;

public record CatalogRecord(RecordId id, Map<String, List<String>> categories, Map<String, List<NameRecord>> teams,
		Map<String, Map<String, String>> translations) {
}
