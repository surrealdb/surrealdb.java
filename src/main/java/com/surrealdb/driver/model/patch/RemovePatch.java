package com.surrealdb.driver.model.patch;

import lombok.Value;

/**
 * @author Khalid Alharisi
 */
@Value
public class RemovePatch implements Patch {

    String op = "remove";
    String path;

}
