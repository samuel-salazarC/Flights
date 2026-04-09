# ✈️ Flights Data Importer

This Java project reads flight data from a CSV file and efficiently inserts it into a MySQL database using batch processing.

---

## 📌 Features

- CSV file reading using OpenCSV
- Data validation (numeric types, null handling)
- Batch insertion for better performance
- Error handling for invalid rows
- Environment variables for database credentials

---

## 🛠️ Technologies

- Java
- MySQL
- OpenCSV
- JDBC

---

## 📂 Project Structure

- `CSVReaderService` → Reads CSV, validates data, and groups records into batches
- `DatabaseService` → Interface for database operations
- `MySQLDatabaseService` → MySQL implementation using JDBC
- `vuelos` → Main class that runs the application

---

## ⚙️ Setup

### 1. Environment Variables

Set the following environment variables:

```bash
export MYSQL_DB_URL=jdbc:mysql://localhost:3306/your_database
export MYSQL_DB_USER=your_user
export MYSQL_DB_PASSWORD=your_password
