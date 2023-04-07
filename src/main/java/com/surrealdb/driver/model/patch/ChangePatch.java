package com.surrealdb.driver.model.patch;

import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
@Getter
public class ChangePatch implements Patch {
	private final String op = "change";
	private final String path;
	private final String value;

	public ChangePatch(String path, String value) {
		this.path = path;
		this.value = value;
	}
}
