package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * @author Khalid Alharisi
 */
@Value
public class ChangePatch implements Patch {

    String op = "change";
    String path;
    String value;

}
