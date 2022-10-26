package com.surrealdb.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.net.URI;

/**
 * A container for all the settings {@link SurrealConnection} needs to connect to a SurrealDB server.
 * Since this class is immutable, it's safe to share it between multiple threads.
 * <p>
 * Use {@code SurrealConnectionSettings.builder()} to create a new instance.
 * If the builder is not configured, it will use the default values.
 * Default values are:
 * <ul>
 *     <li>Protocol: {@link SurrealConnectionProtocol#WEB_SOCKET}</li>
 *     <li>host: localhost</li>
 *     <li>port: 8000</li>
 *     <li>gson: GSON with HTML escaping disabled</li>
 *     <li>auto connect: false</li>
 *     <li>reconnectInterval: 15 seconds</li>
 * </ul>
 *
 * @author Damian Kocher
 */
@Builder(builderClassName = "Builder", setterPrefix = "set")
@Getter
@With
public class SurrealConnectionSettings {

    /**
     * A connection settings instance set to connect to a local SurrealDB server using the default
     * port (8000).
     */
    public static final SurrealConnectionSettings LOCAL_DEFAULT = SurrealConnectionSettings.builder().build();

    @lombok.Builder.Default
    private final URI uri = URI.create("ws://localhost:8000/rpc");

    @lombok.Builder.Default
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    @lombok.Builder.Default
    private final boolean autoConnect = false;

    @lombok.Builder.Default
    private final int defaultConnectTimeoutSeconds = 15;

    /**
     * A builder class for creating a {@link SurrealConnectionSettings} instance. Use {@code SurrealConnectionSettings.builder()}
     * to create a new instance.
     */
    public static class Builder {

        /**
         * Generates and sets the URI from the provided components. This method will override any
         * previously set URI. This method is preferable to {@code .setUri(URI)} because it will ensure
         * the generated URI follows SurrealDB's URI format.
         *
         * @param protocol The protocol to use
         * @param host     The host to connect to
         * @param port     The port to connect to
         * @return this builder
         */
        public Builder setUriFromComponents(SurrealConnectionProtocol protocol, String host, int port) {
            setUri(URI.create(String.format("%s://%s:%d/rpc", protocol.getScheme(), host, port)));
            return this;
        }
    }
}
