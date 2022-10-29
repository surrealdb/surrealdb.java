package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * A patch to change data in an existing record.
 *
 * @author Khalid Alharisi
 */
@Value
public class ChangePatch implements Patch {

    String path;
    String value;

}
