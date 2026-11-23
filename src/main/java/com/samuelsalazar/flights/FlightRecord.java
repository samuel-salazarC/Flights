package com.samuelsalazar.flights;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * One validated row of the flights dataset.
 *
 * <p>Airport coordinates are {@code null} when the source file has no value ("NA" or empty).
 */
public record FlightRecord(
        String originAirport,
        String destinationAirport,
        String originCity,
        String destinationCity,
        int passengers,
        int seats,
        int flights,
        int distance,
        LocalDate flyDate,
        int originPopulation,
        int destinationPopulation,
        BigDecimal originLatitude,
        BigDecimal originLongitude,
        BigDecimal destinationLatitude,
        BigDecimal destinationLongitude) {

    /** Number of columns expected in every CSV row. */
    public static final int COLUMN_COUNT = 15;

    /**
     * Builds a record from a raw CSV row.
     *
     * @throws IllegalArgumentException if the row has the wrong number of columns or a value
     *     cannot be converted to its type
     */
    public static FlightRecord fromCsvRow(String[] row) {
        if (row.length != COLUMN_COUNT) {
            throw new IllegalArgumentException(
                    "expected " + COLUMN_COUNT + " columns but found " + row.length);
        }
        return new FlightRecord(
                row[0].trim(),
                row[1].trim(),
                row[2].trim(),
                row[3].trim(),
                parseInt(row[4], "Passengers"),
                parseInt(row[5], "Seats"),
                parseInt(row[6], "Flights"),
                parseInt(row[7], "Distance"),
                parseDate(row[8]),
                parseInt(row[9], "Origin_population"),
                parseInt(row[10], "Destination_population"),
                parseCoordinate(row[11], "Org_airport_lat"),
                parseCoordinate(row[12], "Org_airport_long"),
                parseCoordinate(row[13], "Dest_airport_lat"),
                parseCoordinate(row[14], "Dest_airport_long"));
    }

    private static int parseInt(String value, String column) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(column + " is not an integer: '" + value + "'");
        }
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fly_date is not a valid date: '" + value + "'");
        }
    }

    private static BigDecimal parseCoordinate(String value, String column) {
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("NA")) {
            return null;
        }
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(column + " is not a number: '" + value + "'");
        }
    }
}
