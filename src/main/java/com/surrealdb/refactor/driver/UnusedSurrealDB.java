package com.surrealdb.refactor.driver;

public interface UnusedSurrealDB <DB extends StatelessSurrealDB>{
    DB use();
    DB use(String namespace);
    DB use(String namespace, String database);
}
