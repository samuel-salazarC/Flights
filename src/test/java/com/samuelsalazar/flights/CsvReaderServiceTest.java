package com.samuelsalazar.flights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvReaderServiceTest {

    private static final String HEADER =
            "\"Origin_airport\",\"Destination_airport\",\"Origin_city\",\"Destination_city\","
                    + "\"Passengers\",\"Seats\",\"Flights\",\"Distance\",\"Fly_date\","
                    + "\"Origin_population\",\"Destination_population\",\"Org_airport_lat\","
                    + "\"Org_airport_long\",\"Dest_airport_lat\",\"Dest_airport_long\"";

    @TempDir Path tempDir;

    /** Keeps the batches in memory instead of writing to a database. */
    private static class InMemoryDatabase implements DatabaseService {
        final List<List<FlightRecord>> batches = new ArrayList<>();

        @Override
        public void insertData(List<FlightRecord> records) {
            batches.add(new ArrayList<>(records));
        }
    }

    private Path writeCsv(String... rows) throws Exception {
        Path file = tempDir.resolve("flights.csv");
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        lines.addAll(List.of(rows));
        Files.write(file, lines);
        return file;
    }

    private static String row(String origin, String passengers, String latitude) {
        return "\"" + origin + "\",\"AMW\",\"Manhattan, KS\",\"Ames, IA\"," + passengers
                + ",30,1,254,2008-10-01,122049,86219," + latitude + ",-96.670799,42.0,-93.62";
    }

    @Test
    void sendsValidRowsInBatchesAndCountsThem() throws Exception {
        Path file = writeCsv(
                row("AAA", "10", "39.1"),
                row("BBB", "20", "39.2"),
                row("CCC", "30", "39.3"),
                row("DDD", "40", "39.4"),
                row("EEE", "50", "39.5"));
        InMemoryDatabase database = new InMemoryDatabase();

        ImportSummary summary = new CsvReaderService(2).importFile(file, database);

        assertEquals(new ImportSummary(5, 5, 0), summary);
        assertEquals(3, database.batches.size());
        assertEquals(2, database.batches.get(0).size());
        assertEquals(1, database.batches.get(2).size());
        assertEquals("EEE", database.batches.get(2).get(0).originAirport());
    }

    @Test
    void skipsInvalidRowsAndKeepsTheRest() throws Exception {
        Path file = writeCsv(
                row("AAA", "10", "39.1"),
                row("BBB", "not-a-number", "39.2"),
                "\"only\",\"two\"",
                row("CCC", "30", "NA"));
        InMemoryDatabase database = new InMemoryDatabase();

        ImportSummary summary = new CsvReaderService().importFile(file, database);

        assertEquals(new ImportSummary(4, 2, 2), summary);
        assertEquals(1, database.batches.size());
        assertNull(database.batches.get(0).get(1).originLatitude());
    }

    @Test
    void quotedCommasStayInsideTheirColumn() throws Exception {
        Path file = writeCsv(row("AAA", "10", "39.1"));
        InMemoryDatabase database = new InMemoryDatabase();

        new CsvReaderService().importFile(file, database);

        assertEquals("Manhattan, KS", database.batches.get(0).get(0).originCity());
    }

    @Test
    void databaseErrorsStopTheImport() throws Exception {
        Path file = writeCsv(row("AAA", "10", "39.1"));
        DatabaseService failingDatabase = records -> {
            throw new SQLException("connection lost");
        };

        assertThrows(
                SQLException.class, () -> new CsvReaderService().importFile(file, failingDatabase));
    }

    @Test
    void rejectsNonPositiveBatchSizes() {
        assertThrows(IllegalArgumentException.class, () -> new CsvReaderService(0));
    }
}
