package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.QueryBlockResult;
import java.util.List;

/** StatelessSurrealDB is the baseline interface available for all SurrealDB instances. */
public interface StatelessSurrealDB {
    /**
     * Perform a general purpose query against the database.
     *
     * @param query
     * @return
     */
    QueryBlockResult query(String query, List<Param> params);
}
