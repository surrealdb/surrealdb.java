package com.surrealdb.driver.model;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

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
