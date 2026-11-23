package com.samuelsalazar.flights;

/** Result of an import: rows read from the file, rows inserted and rows skipped as invalid. */
public record ImportSummary(long rowsRead, long rowsInserted, long rowsSkipped) {}
