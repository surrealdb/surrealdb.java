package com.surrealdb.pojos;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Booleans {

	public boolean primitive;
	public Boolean boxed;
	public Optional<Boolean> opt;
	public Optional<List<String>> optList;

	public Booleans() {
	}

	public Booleans(boolean primitive, Boolean boxed, Optional<Boolean> opt, Optional<List<String>> optList) {
		this.primitive = primitive;
		this.boxed = boxed;
		this.opt = opt;
		this.optList = optList;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final Booleans b = (Booleans) o;
		return primitive == b.primitive && Objects.equals(boxed, b.boxed) && Objects.equals(opt, b.opt)
				&& Objects.equals(optList, b.optList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(primitive, boxed, opt, optList);
	}

	@Override
	public String toString() {
		return "primitive=" + primitive + ", boxed=" + boxed + ", opt=" + opt + ", optList=" + optList;
	}
}
