package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.exception.InvalidAddressException;
import com.surrealdb.refactor.exception.InvalidAddressExceptionCause;
import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.Value;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class HttpConnection {
    public static UnauthenticatedSurrealDB<StatelessSurrealDB> connect(URI uri) {
        List<String> allowed = Arrays.asList("http", "https");
        if (!allowed.contains(uri.getScheme().toLowerCase().trim())) {
            throw new InvalidAddressException(uri, InvalidAddressExceptionCause.INVALID_SCHEME, "Only http and https are supported schemes");
        }

        return new UnauthenticatedSurrealDB<StatelessSurrealDB>() {
            @Override
            public StatelessSurrealDB authenticate(Credentials credentials) {
                return new StatelessSurrealDB() {
                    @Override
                    public List<Value> query(String query, List<Param> params) {
                        throw SurrealDBUnimplementedException.withTicket("https://github.com/surrealdb/surrealdb.java/issues/61").withMessage("HTTP connections are not yet implemented");
                    }

                };
            }
        };
    }
}
