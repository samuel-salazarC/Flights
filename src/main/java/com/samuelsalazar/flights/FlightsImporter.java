package com.samuelsalazar.flights;

import java.nio.file.Files;
import java.nio.file.Path;

/** Command-line entry point: imports a flights CSV file into the MySQL table {@code Vuelos}. */
public class FlightsImporter {

    private static final String DEFAULT_CSV_FILE = "Airports2.csv";

    public static void main(String[] args) {
        Path csvFile = Path.of(args.length > 0 ? args[0] : DEFAULT_CSV_FILE);
        if (!Files.isRegularFile(csvFile)) {
            System.err.println("CSV file not found: " + csvFile.toAbsolutePath());
            System.exit(1);
        }

        String dbUrl = requireEnv("MYSQL_DB_URL");
        String dbUser = requireEnv("MYSQL_DB_USER");
        String dbPassword = requireEnv("MYSQL_DB_PASSWORD");

        long start = System.currentTimeMillis();
        try (MySqlDatabaseService databaseService =
                new MySqlDatabaseService(dbUrl, dbUser, dbPassword)) {
            ImportSummary summary = new CsvReaderService().importFile(csvFile, databaseService);
            long seconds = (System.currentTimeMillis() - start) / 1000;
            System.out.println("Import finished in " + seconds + " s.");
            System.out.println("Rows read:     " + summary.rowsRead());
            System.out.println("Rows inserted: " + summary.rowsInserted());
            System.out.println("Rows skipped:  " + summary.rowsSkipped());
        } catch (Exception e) {
            System.err.println("Import failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            System.err.println("Environment variable " + name + " is not set.");
            System.exit(1);
        }
        return value;
    }
}
