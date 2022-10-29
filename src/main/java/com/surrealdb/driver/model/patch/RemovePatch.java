package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * A patch to remove data from an existing record.
 *
 * @author Khalid Alharisi
 */
@Value
public class RemovePatch implements Patch {

    String path;

}
