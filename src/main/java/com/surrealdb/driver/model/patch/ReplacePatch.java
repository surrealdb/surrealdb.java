package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * @author Khalid Alharisi
 */
@Value
public class ReplacePatch implements Patch {

    String op = "replace";
    String path;
    String value;

}
