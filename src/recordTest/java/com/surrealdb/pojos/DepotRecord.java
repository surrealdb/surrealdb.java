package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record DepotRecord(RecordId id, Map<String, List<Map<String, String>>> sections,
		Map<String, List<List<NameRecord>>> shifts, TreeMap<String, Long> scores) {
}
