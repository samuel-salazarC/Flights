package com.samuelsalazar.flights;

import java.sql.SQLException;
import java.util.List;

/** Destination where batches of validated flight records are stored. */
public interface DatabaseService {

    /** Inserts all the records of a batch, or none of them if the insertion fails. */
    void insertData(List<FlightRecord> records) throws SQLException;
}
