package com.surrealdb;

import com.surrealdb.pojos.ByteData;
import com.surrealdb.pojos.Dates;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Numbers;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeTests {


    @Test
    void testNumberTypes() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new record
            final Numbers n = new Numbers();
            n.longPrimitive = 1;
            n.longObject = 2L;
            n.intPrimitive = 3;
            n.intObject = 4;
            n.shortPrimitive = 5;
            n.shortObject = 6;
            n.floatPrimitive = 7.5f;
            n.floatObject = 8.5f;
            n.doublePrimitive = 9.5f;
            n.doubleObject = 10.5;
            n.bigDecimal = BigDecimal.valueOf(11.5f);
            // We ingest the record
            final Numbers created = surreal.create(Numbers.class, "number", n).get(0);
            // We check that the record are matching
            assertEquals(created, n);
        }
    }

    @Test
    void testDatesTypes() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new record`
            final Dates d = new Dates();
            d.dateTime = ZonedDateTime.ofInstant(Instant.now().minusSeconds(120), ZoneId.of("UTC"));
            d.duration = Duration.ofMinutes(5);
            // We ingest the record
            final Dates created = surreal.create(Dates.class, "date", d).get(0);
            // We check that the records are matching
            assertEquals(created, d);
        }
    }

    @Test
    void testRecordIds() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create content
            final Name name = new Name("foo", "bar");
            // Create record ids
            final RecordId stringId = new RecordId("foo", "bar");
            final RecordId numberId = new RecordId("foo", 25565L);
            final RecordId uuidId = new RecordId("foo", UUID.randomUUID());
            // We ingest the record
            final Value created1 = surreal.create(stringId, name);
            final Value created2 = surreal.create(numberId, name);
            final Value created3 = surreal.create(uuidId, name);
            // We check that the records are matching
            assertEquals(created1.get(Name.class), name);
            assertEquals(created2.get(Name.class), name);
            assertEquals(created3.get(Name.class), name);
        }
    }

    @Test
    void testByteArray() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");

            // Test 1: Create a new record with byte[] data from raw bytes
            final ByteData byteData = new ByteData();
            byteData.data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
            // We ingest the record
            final ByteData created = surreal.create(ByteData.class, "bytedata", byteData).get(0);
            // We check that the records are matching
            assertEquals(created, byteData);

            // Test 2: Create a new record with byte[] data converted from string
            final ByteData byteDataFromString = new ByteData();
            final String testString = "Hello";
            byteDataFromString.data = testString.getBytes();
            // We ingest the record
            final ByteData createdFromString = surreal.create(ByteData.class, "bytedata:string", byteDataFromString).get(0);
            // We check that the records are matching
            assertEquals(createdFromString, byteDataFromString);
            // Verify we can convert the bytes back to string
            final String retrievedString = new String(createdFromString.data);
            assertEquals(retrievedString, testString);

            // Test 3: Select from database and verify byte[] is properly handled
            final Iterator<ByteData> selectedRecords = surreal.select(ByteData.class, "bytedata");
            final ByteData selectedRecord = selectedRecords.next();
            // Compare the byte[] data content
            assertEquals(selectedRecord.data.length, byteData.data.length);
            for (int i = 0; i < selectedRecord.data.length; i++) {
                assertEquals(selectedRecord.data[i], byteData.data[i]);
            }
            // Verify the byte array content is correct
            assertEquals(selectedRecord.data.length, 5);
            assertEquals(selectedRecord.data[0], 0x01);
            assertEquals(selectedRecord.data[4], 0x05);

            // Test 4: Select the string-converted record and verify conversion back to string
            final Iterator<ByteData> selectedStringRecords = surreal.select(ByteData.class, "bytedata:string");
            final ByteData selectedStringRecord = selectedStringRecords.next();
            // Compare the byte[] data content
            assertEquals(selectedStringRecord.data.length, byteDataFromString.data.length);
            for (int i = 0; i < selectedStringRecord.data.length; i++) {
                assertEquals(selectedStringRecord.data[i], byteDataFromString.data[i]);
            }
            final String selectedRetrievedString = new String(selectedStringRecord.data);
            assertEquals(selectedRetrievedString, testString);
        }
    }

}
