package com.surrealdb.refactor.driver;

import com.surrealdb.refactor.exception.InvalidAddressException;
import com.surrealdb.refactor.exception.InvalidAddressExceptionCause;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** SurrealDBFactory is the go-to class for creating new SurrealDB driver instances. */
public class SurrealDBFactory {

    private final Map<String, StatelessProvider> statelessDrivers;
    private final Map<String, BidirectionalProvider> bidirectionalDrivers;

    public SurrealDBFactory() {
        this(getDefaultStatelessDrivers(), getDefaultBidirectionalDrivers());
    }
    public SurrealDBFactory(
            final Map<String, StatelessProvider> statelessDrivers,
            final Map<String, BidirectionalProvider> bidirectionalDrivers) {
        this.statelessDrivers = statelessDrivers;
        this.bidirectionalDrivers = bidirectionalDrivers;
    }

    static Map<String, StatelessProvider> getDefaultStatelessDrivers() {
        final Map<String, StatelessProvider> statelessDrivers = new HashMap<>();
        statelessDrivers.put("http", HttpConnection::connect);
        statelessDrivers.put("https", HttpConnection::connect);
        return statelessDrivers;
    }

    static Map<String, BidirectionalProvider> getDefaultBidirectionalDrivers() {
        final Map<String, BidirectionalProvider> statelessDrivers = new HashMap<>();
        statelessDrivers.put("http", WsPlaintextConnection::connect);
        statelessDrivers.put("https", WsPlaintextConnection::connect);
        statelessDrivers.put("ws", WsPlaintextConnection::connect);
        statelessDrivers.put("wss", WsPlaintextConnection::connect);
        return statelessDrivers;
    }

    public UnauthenticatedSurrealDB<StatelessSurrealDB> connectStateless(final URI uri) {
        return this.statelessDrivers.get(uri.getScheme().toLowerCase().trim()).apply(uri);
    }

    public UnauthenticatedSurrealDB<BidirectionalSurrealDB> connectBidirectional(URI uri) {
        final String key = uri.getScheme().toLowerCase().trim();
        if (!this.bidirectionalDrivers.containsKey(key)) {
            throw new InvalidAddressException(
                    uri,
                    InvalidAddressExceptionCause.INVALID_SCHEME,
                    "Schema is unsupported for bidirectional service");
        }
        if (uri.getPath().isEmpty()) {
            try {
                uri =
                        new URI(
                                uri.getScheme(),
                                uri.getUserInfo(),
                                uri.getHost(),
                                uri.getPort(),
                                uri.getPath() + "/rpc",
                                uri.getQuery(),
                                uri.getFragment());
            } catch (final URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return this.bidirectionalDrivers.get(key).apply(uri);
    }

    interface StatelessProvider
            extends Function<URI, UnauthenticatedSurrealDB<StatelessSurrealDB>> {}

    interface BidirectionalProvider
            extends Function<URI, UnauthenticatedSurrealDB<BidirectionalSurrealDB>> {}
}
