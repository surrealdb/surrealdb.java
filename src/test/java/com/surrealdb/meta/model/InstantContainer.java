package com.surrealdb.meta.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

/**
 * Used for testing instant (de)serialization.
 *
 */
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@Builder
public class InstantContainer {

    @Singular
    List<Instant> instants;

}
