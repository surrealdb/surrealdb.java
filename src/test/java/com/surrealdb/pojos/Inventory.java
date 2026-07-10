package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Inventory {

	public RecordId id;
	public Map<String, List<String>> categories;

	public Inventory() {
	}

	public Inventory(Map<String, List<String>> categories) {
		this.categories = categories;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Inventory inventory = (Inventory) o;
		return Objects.equals(id, inventory.id) && Objects.equals(categories, inventory.categories);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, categories);
	}

	@Override
	public String toString() {
		return "id: " + id + ", categories: " + categories;
	}
}
