package com.surrealdb.driver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author akaecliptic
 */
@Data
@AllArgsConstructor
public class Message {

	private String id;
	private String data;
	private LocalDateTime timestamp;

	public Message(String data, LocalDateTime timestamp) {
		this.data = data;
		this.timestamp = timestamp;
	}
}
