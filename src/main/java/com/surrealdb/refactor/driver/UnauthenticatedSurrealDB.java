package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.types.Credentials;

public interface UnauthenticatedSurrealDB<DB extends StatelessSurrealDB> {
    UnusedSurrealDB<DB> authenticate(Credentials credentials);
}
