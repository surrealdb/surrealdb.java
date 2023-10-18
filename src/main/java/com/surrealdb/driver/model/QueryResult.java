package com.surrealdb.driver.model;

import java.util.List;
import lombok.Data;

/**
 * @author Khalid Alharisi
 */
@Data
public class QueryResult<T> {
    private List<T> result;
    private String status;
    private String time;

    public List<T> getResult() {
        return this.result;
    }

    public void setResult(final List<T> result) {
        this.result = result;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(final String time) {
        this.time = time;
    }
}
