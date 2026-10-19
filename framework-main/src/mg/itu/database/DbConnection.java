package mg.itu.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Small JDBC connection helper. Configure with DB_URL, DB_USER and DB_PASSWORD system properties. */
public final class DbConnection {
    private DbConnection() { }

    public static Connection open() throws SQLException {
        String url = System.getProperty("DB_URL");
        if (url == null || url.isBlank()) throw new IllegalStateException("Set the DB_URL system property before opening a database connection");
        return DriverManager.getConnection(url, System.getProperty("DB_USER", ""), System.getProperty("DB_PASSWORD", ""));
    }
}
