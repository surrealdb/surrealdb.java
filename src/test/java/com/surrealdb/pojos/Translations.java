package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.Map;
import java.util.Objects;

public class Translations {

	public RecordId id;
	public Map<String, Map<String, String>> entries;

	public Translations() {
	}

	public Translations(Map<String, Map<String, String>> entries) {
		this.entries = entries;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Translations translations = (Translations) o;
		return Objects.equals(id, translations.id) && Objects.equals(entries, translations.entries);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, entries);
	}

	@Override
	public String toString() {
		return "id: " + id + ", entries: " + entries;
	}
}
