package com.surrealdb.pojos;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

public class Dates {

    public Duration duration;
    public ZonedDateTime dateTime;

    @Override
    public int hashCode() {
        return Objects.hash(duration, dateTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Dates d = (Dates) o;
        return
            Objects.equals(duration, d.duration) &&
                Objects.equals(dateTime, d.dateTime);
    }

    @Override
    public String toString() {
        return "duration: " + duration + ", dateTime: " + dateTime;
    }

}
