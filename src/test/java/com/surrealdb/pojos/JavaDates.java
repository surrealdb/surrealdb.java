package com.surrealdb.pojos;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

public class JavaDates {

	public Instant instant;
	public OffsetDateTime offsetDateTime;
	public LocalDateTime localDateTime;
	public Date date;

	@Override
	public String toString() {
		return "instant: " + instant + ", offsetDateTime: " + offsetDateTime + ", localDateTime: " + localDateTime
				+ ", date: " + date;
	}

}
