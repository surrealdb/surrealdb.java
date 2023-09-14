package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.exception.InvalidAddressException;
import com.surrealdb.refactor.exception.InvalidAddressExceptionCause;
import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.QueryBlockResult;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class HttpConnection {
    public static UnauthenticatedSurrealDB<StatelessSurrealDB> connect(final URI uri) {
        final List<String> allowed = Arrays.asList("http", "https");
        if (!allowed.contains(uri.getScheme().toLowerCase().trim())) {
            throw new InvalidAddressException(
                    uri,
                    InvalidAddressExceptionCause.INVALID_SCHEME,
                    "Only http and https are supported schemes");
        }

        return new UnauthenticatedSurrealDB<StatelessSurrealDB>() {
            @Override
            public UnusedSurrealDB<StatelessSurrealDB> authenticate(final Credentials credentials) {
                final StatelessSurrealDB surrealdb =
                        new StatelessSurrealDB() {
                            @Override
                            public QueryBlockResult query(
                                    final String query, final List<Param> params) {
                                throw new SurrealDBUnimplementedException(
                                        "https://github.com/surrealdb/surrealdb.java/issues/61",
                                        "HTTP connections are not yet implemented");
                            }
                        };
                return new UnusedSurrealDB<>() {

                    @Override
                    public StatelessSurrealDB use() {
                        throw new SurrealDBUnimplementedException(
                                "https://github.com/surrealdb/surrealdb.java/issues/67",
                                "use is not implemented for HTTP");
                    }

                    @Override
                    public StatelessSurrealDB use(final String namespace) {
                        throw new SurrealDBUnimplementedException(
                                "https://github.com/surrealdb/surrealdb.java/issues/67",
                                "use with namespace is not implemented for HTTP");
                    }

                    @Override
                    public StatelessSurrealDB use(final String namespace, final String database) {
                        throw new SurrealDBUnimplementedException(
                                "https://github.com/surrealdb/surrealdb.java/issues/67",
                                "use with namespace and database is not implemented for HTTP");
                    }
                };
            }
        };
    }
}
