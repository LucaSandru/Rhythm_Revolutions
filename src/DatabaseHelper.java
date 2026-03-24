import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseHelper {
    public static int getUserIDByEmail(String email) {
        String query = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the user is not found
    }

    public static int getArtistIDByName(String artistName) {
        String query = "SELECT id FROM artists WHERE name = ?";
        try (Connection conn = DatabaseConnection.connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, artistName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the artist is not found
    }
}
