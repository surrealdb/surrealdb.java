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
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
