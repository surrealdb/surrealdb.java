package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * A patch to add data to an existing record.
 *
 * @author Khalid Alharisi
 */
@Value
public class AddPatch implements Patch {

    String path;
    String value;

}
