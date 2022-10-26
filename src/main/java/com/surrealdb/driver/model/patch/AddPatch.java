package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * @author Khalid Alharisi
 */
@Value
public class AddPatch implements Patch {

    String op = "add";
    String path;
    String value;

}
