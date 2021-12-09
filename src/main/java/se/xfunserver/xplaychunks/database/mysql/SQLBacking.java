package se.xfunserver.xplaychunks.database.mysql;

import se.xfunserver.xplaychunks.xPlayChunks;

import java.sql.*;
import java.util.function.Supplier;

public class SQLBacking {

    static Connection connect(
            String hostname,
            int port,
            String databaseName,
            String username,
            String password,
            boolean useSsl,
            boolean publicKeyRetrieval)
            throws ClassNotFoundException {
        // Make sure JDBC is loaded
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Create a connection creator with JDBC
        try {
            return DriverManager.getConnection(
                    String.format(
                            "jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=%s",
                            hostname, port, databaseName, useSsl, publicKeyRetrieval),
                    username,
                    password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean getTableDoesntExist(
            xPlayChunks claimChunk,
            Connection connection,
            String databaseName,
            String tableName)
            throws SQLException {
        String sql =
                "SELECT count(*) FROM information_schema.TABLES WHERE (`TABLE_SCHEMA` = ?) AND"
                        + " (`TABLE_NAME` = ?)";
        try (PreparedStatement statement = prep(claimChunk, connection, sql)) {
            statement.setString(1, databaseName);
            statement.setString(2, tableName);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return results.getInt(1) <= 0;
                }
            }
        }
        return true;
    }

    static PreparedStatement prep(
            xPlayChunks claimChunk, Connection connection, String sql)
            throws SQLException {

        return connection.prepareStatement(sql);
    }
}
