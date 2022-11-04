package com.surrealdb.examples;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealConnectionProtocol;
import com.surrealdb.connection.exception.SurrealRecordAlreadyExistsException;
import com.surrealdb.driver.SurrealSyncDriver;
import com.surrealdb.driver.SurrealTable;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.auth.SurrealRootCredentials;
import com.surrealdb.examples.models.Person;

import java.util.Optional;

public class QuickStart {

    public static void main(String[] args) {
        // Create a connection with the minimal amount of configuration
        SurrealConnection connection = SurrealConnection.create(SurrealConnectionProtocol.WEB_SOCKET, "localhost", 8000);
        // If the connection is not established within 15 seconds, an exception will be thrown.
        connection.connect(15);

        // Create a synchronous driver without any driver settings
        SurrealSyncDriver driver = new SurrealSyncDriver(connection);

        // Sign in with the user 'root' and the password 'root'
        SurrealAuthCredentials credentials = SurrealRootCredentials.from("root", "root");

        // Sign in with the newly created credentials
        driver.signIn(credentials);

        // Use the namespace 'examples' and the database 'quickstart'
        driver.use("examples", "quickstart");

        // Create a reference to the "person" table
        // note: Creating a table reference has no effect on the database.
        //       Table references are just wrappers around the table name
        //       and type of object that will be stored within the table.
        SurrealTable<Person> personTable = SurrealTable.create("person", Person.class);

        // Create a new person
        Person tobie = new Person();
        tobie.setTitle("Founder & CEO");
        tobie.setName("Tobie", "Morgan Hitchcock");
        tobie.setMarketing(true);

        try {
            // Save the person to the database
            System.out.println("Saving person to database...");
            driver.create(personTable, "tobie", tobie);
        } catch (SurrealRecordAlreadyExistsException e) {
            // This exception will be thrown if the record already exists
            // in the database. In this case, we will just update the record
            // instead of creating a new one.

            // Try running the program twice to see this behavior in action

            System.out.println("Record already exists, updating instead...");
            driver.updateRecord(personTable, "tobie", tobie);
        }

        // Retrieve the person from the database
        // note: Retrieving a record from the DB returns an Optional. This is to
        //       make it almost impossible to throw a null pointer exception.
        Optional<Person> retrievedTobie = driver.retrieveRecordFromTable(personTable, "tobie");

        // Print the retrieved person
        retrievedTobie.ifPresentOrElse(
                person -> System.out.println("Retrieved person: " + person),
                () -> System.err.println("Failed to retrieve person")
        );

        // Disconnect from the database. This is required in order to exit since
        // the connection is running in a separate thread.
        connection.disconnect();
    }
}
