package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * A patch to add data to an existing record.w
 *
 * @author Khalid Alharisi
 */
@Value
public class AddPatch implements Patch {

    String op = "add";
    String path;
    String value;

}
