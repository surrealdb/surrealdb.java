package com.surrealdb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class BaseIntegrationTest {
    private static Optional<GenericContainer> container = Optional.empty();

    protected static String testHost;
    protected static int testPort;

    @BeforeAll
    public static void set_log_level() {
        //        Logger srdbLog = LogManager.getLogManager();
        //        srdbLog.setLevel(Level.ALL);
        StreamHandler handler = new StreamHandler(System.out, new SimpleFormatter());
        //        srdbLog.addHandler(handler);
        Logger.getGlobal().setLevel(Level.ALL);
        Logger.getGlobal().addHandler(handler);
    }

    @BeforeAll
    public static void create_connection() {
        // Check if both host and port have been decalred as environment variable overrides
        // if they have then use that address instead
        if (Env.envHost.isPresent() && Env.envPort.isPresent()) {
            testHost = Env.envHost.get();
            testPort = Env.envPort.get();
        } else {
            // No env vars, start a container
            container =
                    Optional.of(
                            new GenericContainer(
                                            DockerImageName.parse("surrealdb/surrealdb:latest"))
                                    .withExposedPorts(8000)
                                    .withCommand(
                                            "start --log trace --user root --pass root memory"));
            container.get().start();
            testHost = container.get().getHost();
            testPort = container.get().getFirstMappedPort();
        }
    }

    /**
     * If the connection is http, this will return the http URI
     *
     * @return the URI or empty if it isnt http
     */
    protected Optional<URI> getHttp() {
        // Try env variable
        if (Env.envHost.isPresent() && Env.envPort.isPresent()) {
            return Optional.of(
                    URI.create(
                            String.format("ws://%s:%d/rpc", Env.envHost.get(), Env.envPort.get())));
        }
        // Fallback to container
        if (container.isPresent()) {
            return Optional.of(URI.create("http://" + testHost + ":" + testPort));
        }
        if (testHost.startsWith("http")) {
            try {
                new URI(testHost + ":" + testPort);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    protected Optional<URI> getWebSocketConnection() {
        if (container.isPresent()) {
            return Optional.of(URI.create("ws://" + testHost + ":" + testPort));
        }
        if (testHost.startsWith("ws://") || testHost.startsWith("wss://")) {
            try {
                new URI(testHost + ":" + testPort);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    @AfterAll
    public static void teardown_connection() {
        container.ifPresent(GenericContainer::stop);
    }
}
