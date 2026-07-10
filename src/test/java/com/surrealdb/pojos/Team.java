package com.surrealdb.pojos;

import com.surrealdb.RecordId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Team {

	public RecordId id;
	public Map<String, List<Name>> members;

	public Team() {
	}

	public Team(Map<String, List<Name>> members) {
		this.members = members;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Team team = (Team) o;
		return Objects.equals(id, team.id) && Objects.equals(members, team.members);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, members);
	}

	@Override
	public String toString() {
		return "id: " + id + ", members: " + members;
	}
}
