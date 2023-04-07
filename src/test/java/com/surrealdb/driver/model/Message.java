package com.surrealdb.driver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * @author akaecliptic
 */
@Data
@AllArgsConstructor
public class Message {

	private String id;
	private String data;
	private ZonedDateTime timestamp;

	public Message(String data, ZonedDateTime timestamp) {
		this.data = data;
		this.timestamp = timestamp;
	}
}
