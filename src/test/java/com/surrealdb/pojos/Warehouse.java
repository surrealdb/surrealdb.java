package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Warehouse {

	public RecordId id;
	public Map<String, List<Map<String, String>>> sections;
	public Map<String, List<List<Name>>> shifts;
	public Map<String, List<Integer>> stock;

	public Warehouse() {
	}

	public Warehouse(Map<String, List<Map<String, String>>> sections, Map<String, List<List<Name>>> shifts,
			Map<String, List<Integer>> stock) {
		this.sections = sections;
		this.shifts = shifts;
		this.stock = stock;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Warehouse warehouse = (Warehouse) o;
		return Objects.equals(id, warehouse.id) && Objects.equals(sections, warehouse.sections)
				&& Objects.equals(shifts, warehouse.shifts) && Objects.equals(stock, warehouse.stock);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, sections, shifts, stock);
	}

	@Override
	public String toString() {
		return "id: " + id + ", sections: " + sections + ", shifts: " + shifts + ", stock: " + stock;
	}
}
