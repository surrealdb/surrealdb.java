package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.types.Credentials;

public interface UnauthenticatedSurrealDB<DB extends StatelessSurrealDB>{
    DB authenticate(Credentials credentials);
}
