package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.exception.InvalidAddressException;
import com.surrealdb.refactor.exception.InvalidAddressExceptionCause;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * SurrealDBFactory is the go-to class for creating new SurrealDB driver instances.
 */
public class SurrealDBFactory {

    interface StatelessProvider extends Function<URI, UnauthenticatedSurrealDB<StatelessSurrealDB>> {}
    interface BidirectionalProvider extends Function<URI, UnauthenticatedSurrealDB<BidirectionalSurrealDB>> {}

    private final Map<String, StatelessProvider> statelessDrivers;
    private final Map<String, BidirectionalProvider> bidirectionalDrivers;

    public SurrealDBFactory() {
        this(getDefaultStatelessDrivers(), getDefaultBidirectionalDrivers());
    }

    public SurrealDBFactory(Map<String, StatelessProvider> statelessDrivers, Map<String, BidirectionalProvider> bidirectionalDrivers) {
        this.statelessDrivers = statelessDrivers;
        this.bidirectionalDrivers = bidirectionalDrivers;
    }

    static Map<String, StatelessProvider> getDefaultStatelessDrivers() {
        Map<String, StatelessProvider> statelessDrivers = new HashMap<>();
        statelessDrivers.put("http", HttpConnection::connect);
        statelessDrivers.put("https", HttpConnection::connect);
        return statelessDrivers;
    }

    static Map<String, BidirectionalProvider> getDefaultBidirectionalDrivers() {
        Map<String, BidirectionalProvider> statelessDrivers = new HashMap<>();
        statelessDrivers.put("ws", WsPlaintextConnection::connect);
        statelessDrivers.put("wss", WsPlaintextConnection::connect);
        return statelessDrivers;
    }

    public UnauthenticatedSurrealDB<StatelessSurrealDB> connectStateless(URI uri) {
        return statelessDrivers.get(uri.getScheme().toLowerCase().trim()).apply(uri);
    }

    public UnauthenticatedSurrealDB<BidirectionalSurrealDB> connectBidirectional(URI uri) {
        return bidirectionalDrivers.get(uri.getScheme().toLowerCase().trim()).apply(uri);
    }

}
