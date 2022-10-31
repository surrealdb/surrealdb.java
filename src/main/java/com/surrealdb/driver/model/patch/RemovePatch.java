package com.surrealdb.driver.model.patch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A patch to remove data from an existing record.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class RemovePatch implements Patch {

    /**
     * Used by Gson to serialize the patch.
     */
    private final String op = "remove";

    private final String path;

    public static RemovePatch create(String path) {
        return new RemovePatch(path);
    }

    @Override
    public String getPath() {
        return path;
    }
}
