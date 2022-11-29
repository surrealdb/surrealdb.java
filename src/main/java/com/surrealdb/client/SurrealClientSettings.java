package com.surrealdb.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * A container for all the settings {@link SurrealClient} needs to connect to a SurrealDB server.
 * Since this class is immutable, it's safe to share it between multiple threads.
 * <p>
 * Use {@code SurrealClientSettings.builder()} to create a new instance.
 * If the builder is not configured, it will use the default values.
 * Default values are:
 * <ul>
 *     <li>Protocol: {@link SurrealConnectionProtocol#WEB_SOCKET}</li>
 *     <li>host: localhost</li>
 *     <li>port: 8000</li>
 *     <li>gson: GSON with HTML escaping disabled</li>
 *     <li>logOutgoingRPCs: true</li>
 *     <li>logIncomingResponses: true</li>
 *     <li>logSignInCredentials: false</li>
 * </ul>
 */
@Builder(builderClassName = "Builder", setterPrefix = "set")
@Getter
@With
public final class SurrealClientSettings {

    /**
     * A connection settings instance set to connect to a local SurrealDB server using the default
     * port (8000).
     */
    public static final SurrealClientSettings WEBSOCKET_LOCAL_DEFAULT = SurrealClientSettings.builder().build();

    @lombok.Builder.Default
    @NotNull URI uri = createURI(SurrealConnectionProtocol.WEB_SOCKET, "localhost", 8000);

    @lombok.Builder.Default
    @NotNull Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * Controls whether outgoing messages are logged through SLF4j's {@link org.slf4j.Logger#debug(String)}. If your
     * log level is above debug, this will have no effect as log messages will not be generated at all.  The default
     * value is {@code true}.
     */
    @lombok.Builder.Default
    boolean logOutgoingMessages = true;

    /**
     * Controls whether incoming messages are logged through SLF4j's {@link org.slf4j.Logger#debug(String)}. If your
     * log level is above debug, this will have no effect as log messages will not be generated at all.  The default
     * value is {@code true}.
     */
    @lombok.Builder.Default
    boolean logIncomingMessages = true;

    /**
     * Controls whether authentication credentials are logged. Setting this to {@code true}
     * could potentially pose a security risk. The default value is {@code false}. This setting is ignored if
     * {@link #logOutgoingMessages} is false.
     */
    @lombok.Builder.Default
    boolean logAuthenticationCredentials = false;

    /**
     * The {@link ExecutorService} to use for asynchronous operations. If not provided, a
     * {@link ForkJoinPool} will be used.
     */
    @lombok.Builder.Default
    @NotNull ExecutorService asyncOperationExecutorService = ForkJoinPool.commonPool();

    private static @NotNull URI createURI(@NotNull SurrealConnectionProtocol protocol, @NotNull String host, int port) {
        return URI.create(protocol.getScheme() + "://" + host + ":" + port + "/rpc");
    }

    /**
     * A builder class for creating a {@link SurrealClientSettings} instance. Use {@code SurrealConnectionSettings.builder()}
     * to create a new instance.
     */
    public final static class Builder {

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
        public @NotNull Builder setUriFromComponents(@NotNull SurrealConnectionProtocol protocol, @NotNull String host, int port) {
            setUri(createURI(protocol, host, port));
            return this;
        }
    }
}
