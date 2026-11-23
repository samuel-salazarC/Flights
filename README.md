# Flights Data Importer

Java command-line application that loads a large CSV file of flight records into a MySQL database. It validates every row and inserts the data in batches.

The project was built for a university assignment and uses the USA Airport Dataset: about 3.6 million records of US domestic flights between 1990 and 2009. The full file is not stored in this repository because of its size (about 514 MB); a small sample is included for quick tests.

## Features

- Reads the CSV file as a stream with OpenCSV, so the whole file is never loaded into memory.
- Validates each row: column count, integer fields, dates and coordinates.
- Skips invalid rows, reports them and continues with the rest of the file.
- Stores missing airport coordinates (`NA` or empty) as `NULL`.
- Inserts in batches of 15,000 rows over a single connection, one transaction per batch.
- Reads the database credentials from environment variables.
- Prints a summary with the rows read, inserted and skipped.

## Tech stack

| Tool | Purpose |
|------|---------|
| Java 17 | Core language |
| Maven | Build and dependency management |
| MySQL 8 with Connector/J (JDBC) | Storage |
| OpenCSV | CSV parsing |
| JUnit 5 | Unit tests |

## Project structure

```
Flights/
├── pom.xml
├── data/
│   └── sample_flights.csv          # Ten sample rows in the dataset's format
├── database/
│   └── airport.sql                 # Creates the database and the Vuelos table
└── src/
    ├── main/java/com/samuelsalazar/flights/
    │   ├── FlightsImporter.java        # Entry point
    │   ├── CsvReaderService.java       # Reads, validates and batches the rows
    │   ├── FlightRecord.java           # One validated row
    │   ├── ImportSummary.java          # Result of an import
    │   ├── DatabaseService.java        # Storage interface
    │   └── MySqlDatabaseService.java   # MySQL implementation (JDBC)
    └── test/java/com/samuelsalazar/flights/
        ├── CsvReaderServiceTest.java
        └── FlightRecordTest.java
```

## Setup

### Requirements

- JDK 17 or later
- Maven 3.9 or later
- MySQL 8

### 1. Get the dataset

To try the importer you can use the sample in `data/sample_flights.csv`. Its rows only illustrate the format and are not part of the original dataset.

For the full import, download the [USA Airport Dataset](https://www.kaggle.com/datasets/flashgordon/usa-airport-dataset) from Kaggle (CC0 license). The `Airports2.csv` file used in this project is a cleaned version of that dataset with 3,594,452 rows; the original file also works, because rows that fail validation are skipped and reported. Place the file in the project folder; it is listed in `.gitignore`.

### 2. Create the database

```bash
mysql -u your_user -p < database/airport.sql
```

### 3. Set the environment variables

```bash
export MYSQL_DB_URL="jdbc:mysql://localhost:3306/airport?rewriteBatchedStatements=true"
export MYSQL_DB_USER="your_user"
export MYSQL_DB_PASSWORD="your_password"
```

`rewriteBatchedStatements=true` lets the MySQL driver send each batch as multi-row inserts, which makes the import considerably faster.

### 4. Build

```bash
mvn package
```

This runs the unit tests and creates `target/flights-importer-1.0.0-jar-with-dependencies.jar`.

## Usage

```bash
# Imports the sample file
java -jar target/flights-importer-1.0.0-jar-with-dependencies.jar data/sample_flights.csv

# Imports Airports2.csv from the current directory (default when no file is given)
java -jar target/flights-importer-1.0.0-jar-with-dependencies.jar
```

At the end the program prints how many rows it read, inserted and skipped. It exits with code 1 if the file is missing, an environment variable is not set or the database returns an error.

On a MacBook Pro with a local MySQL 9 server, the full cleaned dataset (3,594,452 rows) was imported in about 40 seconds.

## Tests

```bash
mvn test
```

The tests cover row validation and batching. They use an in-memory implementation of `DatabaseService`, so they do not need a MySQL server.

## Author

Samuel Salazar, Data Science Engineering student. [GitHub profile](https://github.com/samuel-salazarC)
