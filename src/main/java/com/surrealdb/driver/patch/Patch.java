package com.surrealdb.driver.patch;

import org.jetbrains.annotations.NotNull;

/**
 * @author Khalid Alharisi
 */
public sealed interface Patch permits AddPatch, ChangePatch, RemovePatch, ReplacePatch {

    @NotNull String getPath();

}
