package com.surrealdb.refactor.driver;

/**
 * An API that forces the user to decide whether to use a namespace, database, token or none.
 * Usually, as a user connects, they need to first authenticate, then configure (such as USE), then they have access.
 *
 * @param <DB>
 */
public interface UnusedSurrealDB <DB extends StatelessSurrealDB>{
    DB use();
    DB use(String namespace);
    DB use(String namespace, String database);
}
