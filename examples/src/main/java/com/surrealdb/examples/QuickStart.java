package com.surrealdb.examples;

import com.surrealdb.*;
import com.surrealdb.auth.SurrealAuthCredentials;
import com.surrealdb.auth.SurrealRootCredentials;
import com.surrealdb.examples.models.Person;
import com.surrealdb.exception.SurrealRecordAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class QuickStart {

    public static void main(String[] args) {
        // Create a client with the minimal amount of configuration
        BiDirectionalSurrealClient client = WebSocketSurrealClient.create(SurrealClientSettings.WEBSOCKET_LOCAL_DEFAULT);

        // Connect to the server. An exception will be thrown if it takes longer than 5 seconds to connect
        client.connect(5, TimeUnit.SECONDS);

        // Sign in with the user 'root' and the password 'root'
        SurrealAuthCredentials credentials = SurrealRootCredentials.from("root", "root");

        // Sign in with the newly created credentials
        client.signIn(credentials);

        // Use the namespace 'examples' and the database 'quickstart'
        client.use("examples", "quickstart");

        // Create a reference to the "person" table
        // note: Creating a table reference has no effect on the database.
        //       Table references are just wrappers around the table name
        //       and type of object that will be stored within the table.
        SurrealTable<Person> personTable = SurrealTable.of("person", Person.class);

        // Create a new person
        Person tobie = new Person();
        tobie.setTitle("Founder & CEO");
        tobie.setName("Tobie", "Morgan Hitchcock");
        tobie.setMarketing(true);

        try {
            // Save the person to the database
            log.info("Saving person to the database...");
            client.createRecord(personTable, "tobie", tobie);
        } catch (SurrealRecordAlreadyExistsException e) {
            // This exception will be thrown if the record already exists
            // in the database. In this case, we will just update the record
            // instead of creating a new one.

            // Try running the program twice to see this behavior in action

            log.info("Record already exists, updating instead...");
            client.updateRecord(personTable, "tobie", tobie);
        }

        // Retrieve the person from the database
        // note: Retrieving a record from the DB returns an Optional. This is to
        //       make it almost impossible to throw a null pointer exception.
        Optional<Person> retrievedTobie = client.retrieveRecord(personTable, "tobie");

        // Print the retrieved person
        retrievedTobie.ifPresentOrElse(
            person -> log.info("Retrieved person: {}", person),
            () -> log.error("Failed to retrieve person")
        );

        // Disconnect from the database. This is required in order to exit since
        // the connection is running in a separate thread.
        client.disconnect();
    }
}
