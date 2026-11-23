package com.samuelsalazar.flights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FlightRecordTest {

    private static String[] validRow() {
        return new String[] {
            "MHK", "AMW", "Manhattan, KS", "Ames, IA", "21", "30", "1", "254", "2008-10-01",
            "122049", "86219", "39.140999", "-96.670799", "42.0", "-93.62"
        };
    }

    @Test
    void parsesAValidRow() {
        FlightRecord record = FlightRecord.fromCsvRow(validRow());

        assertEquals("MHK", record.originAirport());
        assertEquals("Ames, IA", record.destinationCity());
        assertEquals(21, record.passengers());
        assertEquals(254, record.distance());
        assertEquals(LocalDate.of(2008, 10, 1), record.flyDate());
        assertEquals(86219, record.destinationPopulation());
        assertEquals(new BigDecimal("-96.670799"), record.originLongitude());
    }

    @Test
    void missingCoordinatesBecomeNull() {
        String[] row = validRow();
        row[11] = "NA";
        row[12] = "";

        FlightRecord record = FlightRecord.fromCsvRow(row);

        assertNull(record.originLatitude());
        assertNull(record.originLongitude());
        assertEquals(new BigDecimal("42.0"), record.destinationLatitude());
    }

    @Test
    void rejectsRowsWithTheWrongNumberOfColumns() {
        String[] row = {"MHK", "AMW"};

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> FlightRecord.fromCsvRow(row));
        assertTrue(error.getMessage().contains("15 columns"));
    }

    @Test
    void rejectsNonNumericValues() {
        String[] row = validRow();
        row[10] = "unknown";

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> FlightRecord.fromCsvRow(row));
        assertTrue(error.getMessage().contains("Destination_population"));
    }

    @Test
    void rejectsInvalidDates() {
        String[] row = validRow();
        row[8] = "2008-13-45";

        assertThrows(IllegalArgumentException.class, () -> FlightRecord.fromCsvRow(row));
    }
}
