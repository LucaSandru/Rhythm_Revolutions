import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User implements Editable, Comparable<User> {
    private String name;
    private String password;
    private String email;
    private List<Playlist> playlists;
    private List<String> followedArtists;
    private int followedArtistsCount;
    private LocalDateTime accountCreated;
    private Set<Genre> interactedGenres;  // New Set to track unique genres
    private int id;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // For parsing from file

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.playlists = new ArrayList<>();
        this.followedArtists = new ArrayList<>();
        this.followedArtistsCount = 0;
        this.accountCreated = LocalDateTime.now();
        this.interactedGenres = new HashSet<>();  // Initialize the Set
        this.id = fetchUserId(email);
    }

    public int getId() {
        return this.id;
    }

    private int fetchUserId(String email) {
        String query = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            if (conn != null) {
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if the user ID is not found
    }


    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getFormattedAccountCreated() {
        return accountCreated.format(FORMATTER);
    }

    public List<String> getFollowedArtists() {
        return Collections.unmodifiableList(followedArtists);
    }

    public Set<Genre> getInteractedGenres() {
        return Collections.unmodifiableSet(interactedGenres);
    }

    public int compareTo(User otherUser) {
        return this.accountCreated.compareTo(otherUser.accountCreated);
    }

    public void followArtist(String artistName) {
        if (!followedArtists.contains(artistName)) {
            followedArtists.add(artistName);
        }
    }

    // ✅ Update User Email
    public boolean updateUserEmail(String newEmail) {
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            String sql = "UPDATE users SET email = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newEmail);
            stmt.setInt(2, this.id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                this.email = newEmail;
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Delete User Account
    public boolean deleteUserAccount() {
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            String sql = "DELETE FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void unfollowArtist(Artist artist) {
        if (followedArtists.contains(artist.getName())) {
            followedArtists.remove(artist.getName());
            followedArtistsCount--;
            System.out.println(name + " unfollowed artist: " + artist.getName());
        } else {
            System.out.println(artist.getName() + " is not in the followed list of " + name);
        }
    }

    public void interactWithGenre(Genre genre) {
        interactedGenres.add(genre);  // Add genre to Set
        System.out.println("User " + name + " has interacted with genre: " + genre.getGenreName());
    }

    public void displayInteractedGenres() {
        System.out.println("Genres interacted by user " + name + ":");
        for (Genre genre : interactedGenres) {
            genre.displayGenre();
        }
    }

    public void edit(String username) {
        if (!this.name.equals(username)) {
            this.name = username;
            System.out.println("User name successfully updated to: " + name);
        } else {
            System.out.println("Username already exists: " + name);
        }
    }

    public void updateEmail(String email) {
        if (!this.email.equals(email)) {
            this.email = email;
            System.out.println("Email successfully updated to: " + email);
        } else {
            System.out.println("The email is already associated with this account: " + email);
        }
    }

    public void changePassword(String password) {
        if (!this.password.equals(password)) {
            this.password = password;
            System.out.println("Password successfully updated.");
        } else {
            System.out.println("This password is already in use.");
        }
    }

    public void displayUserInfo() {
        System.out.println("Account Details:");
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Followed Artists: " + followedArtists);
        System.out.println("Account Created: " + getFormattedAccountCreated());
        displayInteractedGenres();  // Display genres
    }

    public void addPlaylist(Playlist playlist) {
        if (!playlists.contains(playlist)) {
            playlists.add(playlist);
        }
    }

    public List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(playlists);
    }

    public List<String> getPlaylistNames() {
        List<String> playlistNames = new ArrayList<>();
        for (Playlist playlist : playlists) {
            playlistNames.add(playlist.getName());  // Fixed this line too
        }
        return playlistNames;
    }

    public String toFileString() {
        String formattedPlaylists = playlists.stream()
                .map(Playlist::getName)  // Fixed this line
                .reduce((p1, p2) -> p1 + ";" + p2).orElse("");

        return String.join(",", name, password, email, accountCreated.format(FILE_FORMATTER), formattedPlaylists);
    }


    public static User fromFileString(String fileString) {
        String[] parts = fileString.split(",");
        if (parts.length < 4) return null;

        User user = new User(parts[0], parts[1], parts[2]);
        user.accountCreated = LocalDateTime.parse(parts[3], FILE_FORMATTER);

        // If playlists are present, add them
        if (parts.length > 4) {
            String[] playlistNames = parts[4].split(";");
            for (String playlistName : playlistNames) {
                user.addPlaylist(new Playlist(playlistName));
            }
        }
        return user;
    }
}
