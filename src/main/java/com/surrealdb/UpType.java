package com.surrealdb;

/**
 * Enumeration to represent the type of update operations.
 * <p>
 * The UpType enum provides constants to specify the kind of update operation that should be performed.
 */
public enum UpType {

    /**
     * Represents a content update operation.
     * This type of operation replaces the entire existing data with the provided data.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update#content-clause">SurrealQL documentation</a>.
     * <p>
     */
    CONTENT(1),
    /**
     * Represents a merge update operation.
     * This type of operation merges the existing data with the provided data.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update#merge-clause">SurrealQL documentation</a>.
     * <p>
     */
    MERGE(2),
    /**
     * Represents a patch update operation.
     * This type of operation applies partial changes to the existing data.
     * <p>
     * For more details, check the <a href="https://surrealdb.com/docs/surrealql/statements/update#patch-clause">SurrealQL documentation</a>.
     * <p>
     */
    PATCH(3);

    final int code;

    UpType(int code) {
        this.code = code;
    }

}
