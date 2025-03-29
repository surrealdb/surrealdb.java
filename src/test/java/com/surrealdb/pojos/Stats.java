package com.surrealdb.pojos;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Objects;

public class Stats {

    public HashMap<String, Long> statistics;
    public HashMap<String, Dates> sessions;

    @Override
    public int hashCode() {
        return Objects.hash(statistics, sessions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Stats d = (Stats) o;
        return
            Objects.equals(statistics, d.statistics) &&
                Objects.equals(sessions, d.sessions);
    }

    @Override
    public String toString() {
        return "statistics: " + statistics + ", sessions: " + sessions;
    }

}
