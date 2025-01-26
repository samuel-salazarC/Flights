//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class MySQLDatabaseService implements DatabaseService {
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private static final int BATCH_SIZE = 500;

    public MySQLDatabaseService(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public void insertData(List<String[]> data) throws SQLException {
        String insertQuery = "\n        INSERT INTO Vuelos (\n    Origin_airport,\n    Destination_airport,\n    Origin_city,\n    Destination_city,\n    Passengers,\n    Seats,\n    Flights,\n    Distance,\n    Fly_date,\n    Origin_population,\n    Destination_population,\n    Org_airport_lat,\n    Org_airport_long,\n    Dest_airport_lat,\n    Dest_airport_long\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\n";

        try (
                Connection conn = DriverManager.getConnection(this.dbUrl, this.dbUser, this.dbPassword);
                PreparedStatement pstmt = conn.prepareStatement(insertQuery);
        ) {
            int count = 0;

            for(String[] row : data) {
                try {
                    pstmt.setString(1, row[0]);
                    pstmt.setString(2, row[1]);
                    pstmt.setString(3, row[2]);
                    pstmt.setString(4, row[3]);
                    pstmt.setInt(5, Integer.parseInt(row[4]));
                    pstmt.setInt(6, Integer.parseInt(row[5]));
                    pstmt.setInt(7, Integer.parseInt(row[6]));
                    pstmt.setInt(8, Integer.parseInt(row[7]));
                    pstmt.setDate(9, Date.valueOf(row[8]));
                    pstmt.setInt(10, Integer.parseInt(row[9]));
                    pstmt.setInt(11, Integer.parseInt(row[10]));
                    pstmt.setBigDecimal(12, this.validateBigDecimal(row[11]));
                    pstmt.setBigDecimal(13, this.validateBigDecimal(row[12]));
                    pstmt.setBigDecimal(14, this.validateBigDecimal(row[13]));
                    pstmt.setBigDecimal(15, this.validateBigDecimal(row[14]));
                    pstmt.addBatch();
                    ++count;
                    if (count % 500 == 0) {
                        pstmt.executeBatch();
                        System.out.println("Insertando " + count + " filas...");
                    }
                } catch (NumberFormatException e) {
                    PrintStream var10000 = System.err;
                    String var10001 = Arrays.toString(row);
                    var10000.println("Error al convertir fila: " + var10001 + " - " + e.getMessage());
                }
            }

            pstmt.executeBatch();
            System.out.println("Datos insertados correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private BigDecimal validateBigDecimal(String value) {
        return value != null && !value.isEmpty() && !value.equals("N") ? new BigDecimal(value) : BigDecimal.ZERO;
    }
}
