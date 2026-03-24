import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/music_app";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "sandruluca2004";

    public static Connection connectDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // Ensure driver is loaded
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
