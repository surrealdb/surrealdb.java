package com.surrealdb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the SurrealDB object key used when converting a Java field.
 *
 * <p>
 * Unannotated fields keep the historical behavior and use the Java field name.
 * For records, annotate the record component; the field-target annotation is
 * available on the generated backing field used by the converter.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SurrealName {

	/**
	 * The SurrealDB object key to use for this field.
	 *
	 * @return a non-blank SurrealDB object key
	 */
	String value();
}
