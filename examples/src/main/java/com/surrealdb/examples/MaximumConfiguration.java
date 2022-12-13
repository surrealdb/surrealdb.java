package com.surrealdb.examples;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.client.SurrealBiDirectionalClient;
import com.surrealdb.client.bidirectional.SurrealWebSocketClient;
import com.surrealdb.client.settings.SurrealClientSettings;
import com.surrealdb.client.settings.SurrealConnectionProtocol;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MaximumConfiguration {

    public static void main(String[] args) {
        SurrealClientSettings settings = createClientSettings();
        SurrealBiDirectionalClient client = SurrealWebSocketClient.create(settings);

        client.connect(5, TimeUnit.SECONDS);

        // ... insert business logic here ...

        // Disconnect to shut down the client thread
        client.disconnect();
        // Shut down the executor service to prevent the program from hanging
        // This is only needed if you are using a custom executor service with without daemon threads
        client.getAsyncOperationExecutorService().shutdown();
    }

    private static SurrealClientSettings createClientSettings() {
        // Provide your own Gson instance to customize the serialization of your data
        Gson gson = new GsonBuilder()
            // Register your custom type adapters here
            .create();

        // Clients use an ExecutorService to run async tasks. You can provide your own ExecutorService,
        // or you can leave the setting unchanged to use Java's fork-join common pool.
        // All implementations of ExecutorService are supported. Benchmarking is recommended to determine
        // which implementation is best for your use case.
        ExecutorService asyncExecutor = Executors.newCachedThreadPool();

        /*
         * ExecutorService asyncExecutor = Executors.newSingleThreadExecutor(); // This is NOT recommended
         * ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);
         * ExecutorService asyncExecutor = Executors.newWorkStealingPool();
         */

        return SurrealClientSettings.builder()
            // Conveniently specify the host, and port to connect to, along with the protocol to use
            // This example will evaluate to "ws://localhost:8000/rpc"
            .setUriFromComponents(SurrealConnectionProtocol.WEB_SOCKET, "localhost", 8000)
            // If you want more control over the URI you can use this method instead:
            .setUri(URI.create("ws://localhost:8000/rpc"))
            // Set our custom Gson instance
            .setGson(gson)
            // Enable/disable logging incoming messages
            .setLogIncomingMessages(true)
            // Enable/disable logging outgoing messages
            .setLogOutgoingMessages(true)
            // Enable/disable logging authentication credentials
            // Disabling this is recommended if there is a chance that logs will be exposed
            .setLogAuthenticationCredentials(false)
            // Set the executor service to use for async operations
            .setAsyncOperationExecutorService(asyncExecutor)
            .build();
    }
}
