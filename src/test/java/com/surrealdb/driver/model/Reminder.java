package com.surrealdb.driver.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author akaecliptic
 */
@Data
@AllArgsConstructor
public class Reminder {

    private String id;
    private String note;
    private LocalDateTime time;

    public Reminder(String note, LocalDateTime time) {
        this.note = note;
        this.time = time;
    }
}
