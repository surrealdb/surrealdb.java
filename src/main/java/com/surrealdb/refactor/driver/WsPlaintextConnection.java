package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.exception.SurrealDBUnimplementedException;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.Value;

import java.net.URI;
import java.util.List;

public class WsPlaintextConnection {
    public static UnauthenticatedSurrealDB<BidirectionalSurrealDB> connect(URI uri) {
        return new UnauthenticatedSurrealDB<BidirectionalSurrealDB>() {
            @Override
            public BidirectionalSurrealDB authenticate(Credentials credentials) {
                return new BidirectionalSurrealDB() {

                    @Override
                    public List<Value> query(String query, List<Param> params) {
                        throw SurrealDBUnimplementedException.withTicket("TODO create ticket").withMessage("Plaintext websocket connections are not supported yet");
                    }
                };
            }
        };
    }
}
