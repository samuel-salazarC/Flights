package com.samuelsalazar.flights;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reads the flights CSV, validates every row and sends the valid ones to the database in batches. */
public class CsvReaderService {

    public static final int DEFAULT_BATCH_SIZE = 15_000;

    /** Invalid rows reported individually; the rest are only counted. */
    private static final int MAX_REPORTED_ERRORS = 20;

    private final int batchSize;

    public CsvReaderService() {
        this(DEFAULT_BATCH_SIZE);
    }

    public CsvReaderService(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        this.batchSize = batchSize;
    }

    /**
     * Imports the file into the database.
     *
     * @return how many rows were read, inserted and skipped
     */
    public ImportSummary importFile(Path csvFile, DatabaseService databaseService)
            throws IOException, CsvValidationException, SQLException {
        long rowsRead = 0;
        long rowsInserted = 0;
        long rowsSkipped = 0;
        List<FlightRecord> batch = new ArrayList<>(batchSize);

        try (Reader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8);
                CSVReader csvReader = new CSVReader(reader)) {
            csvReader.readNext(); // header

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                rowsRead++;
                try {
                    batch.add(FlightRecord.fromCsvRow(row));
                } catch (IllegalArgumentException e) {
                    rowsSkipped++;
                    if (rowsSkipped <= MAX_REPORTED_ERRORS) {
                        System.err.println("Row " + (rowsRead + 1) + " skipped: " + e.getMessage());
                    }
                    continue;
                }

                if (batch.size() >= batchSize) {
                    databaseService.insertData(batch);
                    rowsInserted += batch.size();
                    batch.clear();
                    System.out.println(rowsInserted + " rows inserted...");
                }
            }

            if (!batch.isEmpty()) {
                databaseService.insertData(batch);
                rowsInserted += batch.size();
            }
        }

        if (rowsSkipped > MAX_REPORTED_ERRORS) {
            System.err.println(
                    (rowsSkipped - MAX_REPORTED_ERRORS) + " more invalid rows were skipped.");
        }
        return new ImportSummary(rowsRead, rowsInserted, rowsSkipped);
    }
}
