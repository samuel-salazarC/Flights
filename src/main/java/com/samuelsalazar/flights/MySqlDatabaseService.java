package com.samuelsalazar.flights;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * MySQL implementation that keeps one connection open for the whole import and commits each
 * batch as a single transaction.
 */
public class MySqlDatabaseService implements DatabaseService, AutoCloseable {

    private static final String INSERT_SQL =
            """
            INSERT INTO Vuelos (
                Origin_airport, Destination_airport, Origin_city, Destination_city,
                Passengers, Seats, Flights, Distance, Fly_date,
                Origin_population, Destination_population,
                Org_airport_lat, Org_airport_long, Dest_airport_lat, Dest_airport_long
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final Connection connection;

    public MySqlDatabaseService(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        this.connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        this.connection.setAutoCommit(false);
    }

    @Override
    public void insertData(List<FlightRecord> records) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            for (FlightRecord record : records) {
                statement.setString(1, record.originAirport());
                statement.setString(2, record.destinationAirport());
                statement.setString(3, record.originCity());
                statement.setString(4, record.destinationCity());
                statement.setInt(5, record.passengers());
                statement.setInt(6, record.seats());
                statement.setInt(7, record.flights());
                statement.setInt(8, record.distance());
                statement.setDate(9, Date.valueOf(record.flyDate()));
                statement.setInt(10, record.originPopulation());
                statement.setInt(11, record.destinationPopulation());
                setCoordinate(statement, 12, record.originLatitude());
                setCoordinate(statement, 13, record.originLongitude());
                setCoordinate(statement, 14, record.destinationLatitude());
                setCoordinate(statement, 15, record.destinationLongitude());
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    private static void setCoordinate(PreparedStatement statement, int index, BigDecimal value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.DECIMAL);
        } else {
            statement.setBigDecimal(index, value);
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
