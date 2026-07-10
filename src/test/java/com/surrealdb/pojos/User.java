package com.surrealdb.pojos;

import java.util.Objects;

import com.surrealdb.RecordId;

public class User {

	public RecordId id;
	public String email;

	public User() {
	}

	public User(RecordId id, String email) {
		this.id = id;
		this.email = email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final User user = (User) o;
		return Objects.equals(id, user.id) && Objects.equals(email, user.email);
	}

	@Override
	public String toString() {
		return "id: " + id + ", email: " + email;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, email);
	}
}
