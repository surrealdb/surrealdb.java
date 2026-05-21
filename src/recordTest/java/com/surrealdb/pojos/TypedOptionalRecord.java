package com.surrealdb.pojos;

import java.util.Optional;

public record TypedOptionalRecord(Optional<Integer> intOpt, Optional<Float> floatOpt, Optional<Short> shortOpt,
		Optional<Long> longOpt) {
}
