package com.surrealdb.driver.model.patch;

import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
@Getter
public class AddPatch implements Patch {
	private final String op = "add";
	private final String path;
	private final String value;

	public AddPatch(String path, String value) {
		this.path = path;
		this.value = value;
	}
}
