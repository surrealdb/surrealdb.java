package com.surrealdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.surrealdb.pojos.EmailRecord;
import com.surrealdb.pojos.NameRecord;
import com.surrealdb.pojos.PersonRecord;

final class RecordHelpers {

	static final PersonRecord TOBIE = new PersonRecord(null, "Tobie", Arrays.asList("CEO", "CTO"), 1L, true,
			Collections.singletonList(new EmailRecord("tobie@example.com", new NameRecord("Tobie", "Foo"))),
			Optional.of("toby"));

	static final PersonRecord JAIME = new PersonRecord(null, "Jaime", Collections.singletonList("COO"), 2L, true,
			Collections.singletonList(new EmailRecord("jaime@example.com", new NameRecord("Jaime", "Bar"))),
			Optional.empty());

	static PersonRecord withoutId(PersonRecord p) {
		return new PersonRecord(null, p.name(), p.tags(), p.category(), p.active(), p.emails(), p.nickname());
	}

	private RecordHelpers() {
	}
}
