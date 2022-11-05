package com.surrealdb.examples;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealConnectionProtocol;
import com.surrealdb.connection.SurrealConnectionSettings;
import com.surrealdb.driver.SurrealDriver;
import com.surrealdb.driver.SurrealDriverSettings;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MaximumConfiguration {

    public static void main(String[] args) {
        SurrealConnectionSettings connectionSettings = createConnectionSettings();
        SurrealConnection connection = SurrealConnection.create(connectionSettings);

        SurrealDriverSettings driverSettings = createDriverSettings();
        SurrealDriver driver = SurrealDriver.create(connection, driverSettings);

        // ... insert business logic here ...

        // Disconnect to shut down the connection thread
        connection.disconnect();
        // Shut down the executor service to prevent the program from hanging
        // This is only needed if you are using a custom executor service with without daemon threads
        driver.getAsyncOperationExecutorService().shutdown();
    }

    private static SurrealConnectionSettings createConnectionSettings() {
        // Provide your own Gson instance to customize the serialization of your data
        Gson gson = new GsonBuilder()
            // Register your custom type adapters here
            .create();

        return SurrealConnectionSettings.builder()
            // Conveniently specify the host, and port to connect to, along with the protocol to use
            // This example will evaluate to "ws://localhost:8000/rpc"
            .setUriFromComponents(SurrealConnectionProtocol.WEB_SOCKET, "localhost", 8000)
            // If you want more control over the URI you can use this method instead:
            .setUri(URI.create("ws://localhost:8000/rpc"))
            // Set our custom Gson instance
            .setGson(gson)
            // Enable/disable logging outgoing messages
            .setLogIncomingMessages(true)
            // Enable/disable logging incoming messages
            .setLogOutgoingMessages(true)
            // Enable/disable logging authentication credentials
            // Disabling this is recommended if there is a chance that logs will be exposed
            .setLogAuthenticationCredentials(false)
            // Set the default timeout for connection attempts. This is the time to wait for a connection to be established
            // before giving up and throwing an exception.
            .setDefaultConnectTimeoutSeconds(15)
            // If enabled, the SurrealConnection will automatically connect to the server when it's created.
            // If disabled, you will need to call connect() manually.
            .setAutoConnect(true)
            .build();
    }

    private static SurrealDriverSettings createDriverSettings() {
        // Drivers use executor services to run async tasks. You can provide your own executor service,
        // or you can leave the setting unchanged to use Java's fork-join common pool.
        // All implementations of ExecutorService are supported. Benchmarking is recommended to determine
        // which implementation is best for your use case.
        ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();
        /*
         * ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);
         * ExecutorService asyncExecutor = Executors.newCachedThreadPool();
         * ExecutorService asyncExecutor = Executors.newWorkStealingPool();
         */

        return SurrealDriverSettings.builder()
            .setAsyncOperationExecutorService(asyncExecutor)
            .build();
    }
}
