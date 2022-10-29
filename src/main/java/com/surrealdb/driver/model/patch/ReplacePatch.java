package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * A patch to modify data in an existing record.
 *
 * @author Khalid Alharisi
 */
@Value
public class ReplacePatch implements Patch {

    String path;
    String value;

}
