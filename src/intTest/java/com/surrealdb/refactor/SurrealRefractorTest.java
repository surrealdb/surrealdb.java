package com.surrealdb.refactor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.TestUtils;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.Person;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.refactor.driver.parsing.ResultParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SurrealRefractorTest extends BaseIntegrationTest {

    private SyncSurrealDriver driver;
    private ResultParser resultParser;

    public SurrealRefractorTest() {
        // TODO Auto-generated constructor stub
    }

    @BeforeEach
    public void setup() {
        SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(testHost, testPort, false);
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);

        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.create("person:1", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.create("person:2", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        driver.delete("person");
        driver.delete("movie");
        driver.delete("message");
        driver.delete("reminder");
    }

    @Test
    public void testSingleQueryResult() {
        // declarations
        com.surrealdb.refactor.types.QueryResult[] processedOuterResults;
        Gson gson = new Gson();
        this.resultParser = new ResultParser();

        // given
        StringBuilder query = new StringBuilder("Create person SET title = 'Founder & CEO', ");
        query.append("name.first = 'Tobie', name.last = 'Morgan Hitchcock', marketing = 'true' \n");
        // surrealDB.query
        Map<String, String> args = new HashMap<>();
        List<QueryResult<Person>> response = driver.query(query.toString(), args, Person.class);
        Person singlePerson = response.get(0).getResult().get(0);


        // when
        String resultString = gson.toJson(singlePerson);
        JsonElement results = JsonParser.parseString(resultString);
        processedOuterResults = resultParser.parseResultMessage(results);

        driver.delete("person");

        // then
        assertEquals(1, processedOuterResults.length);
    }
}
