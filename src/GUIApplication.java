import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.util.Arrays;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;



public class GUIApplication extends Application {
    private static final String ADMIN_PASSWORD = "sandruluca2004";
    private static final String ARTISTS_FILE = "artists.txt";
    private Stage stage;
    private User currentUser;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = 0;
    private List<File> playlistSongs = new ArrayList<>();
    private Label songTimeLabel = new Label("00:00");
    private Label currentSongLabel = new Label("No song playing");
    private MediaPlayer currentMediaPlayer;




    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        showRoleSelectionScene(); // Start with the role selection scene
    }

    private Connection connectDatabase() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/music_app", "root", "sandruluca2004");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }




    private void showRoleSelectionScene() {
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        try {
            Image adminImage = new Image(getClass().getResource("admin_icon.png").toExternalForm());
            Image userImage = new Image(getClass().getResource("user_icon.png").toExternalForm());

            ImageView adminImageView = new ImageView(adminImage);
            adminImageView.setFitWidth(120);
            adminImageView.setFitHeight(120);
            adminImageView.setPreserveRatio(true);
            adminImageView.setOnMouseClicked(e -> showAdminLoginScene());

            ImageView userImageView = new ImageView(userImage);
            userImageView.setFitWidth(120);
            userImageView.setFitHeight(120);
            userImageView.setPreserveRatio(true);
            userImageView.setOnMouseClicked(e -> showRegistrationScene());

            Label adminLabel = new Label("Admin");
            adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label userLabel = new Label("User");
            userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            VBox adminBox = new VBox(10, adminImageView, adminLabel);
            adminBox.setAlignment(Pos.CENTER);

            VBox userBox = new VBox(10, userImageView, userLabel);
            userBox.setAlignment(Pos.CENTER);

            HBox hbox = new HBox(50, adminBox, userBox);
            hbox.setAlignment(Pos.CENTER);

            Label welcomeLabel = new Label("Welcome to Rhythm Revolutions!");
            welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            vbox.getChildren().addAll(welcomeLabel, hbox);


        } catch (NullPointerException e) {
            System.err.println("Error loading images. Please check the file paths.");
            e.printStackTrace();
        }

        Scene scene = new Scene(vbox, 500, 400);
        setScene(scene, "Role Selection");
    }


    private void showRegistrationScene() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);


        Label titleLabel = new Label("User Registration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        Button switchToLoginButton = new Button("Switch to Login");
        switchToLoginButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");

        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        Label feedbackLabel = new Label();

        registerButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String password = passwordField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || password.isEmpty() || email.isEmpty()) {
                feedbackLabel.setText("Please fill out all fields.");
                return;
            }

            if (password.length() < 4) {
                feedbackLabel.setText("Password must have at least 4 characters.");
                return;
            }

            if (!isValidEmail(email)) {
                feedbackLabel.setText("Invalid email format. Please enter a valid email.");
                return;
            }

            try (Connection conn = connectDatabase()) {
                String sql = "INSERT INTO users (name, password, email, date) VALUES (?, ?, ?, NOW())";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, password);
                stmt.setString(3, email);
                stmt.executeUpdate();
                feedbackLabel.setText("User registered successfully!");
            } catch (SQLException ex) {
                feedbackLabel.setText("User registration failed. Email might already be in use.");
                ex.printStackTrace();
            }
        });

        switchToLoginButton.setOnAction(e -> showLoginScene());
        quitButton.setOnAction(e -> showRoleSelectionScene()); // Return to role selection screen

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Name:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(registerButton, 0, 4);
        grid.add(switchToLoginButton, 1, 4);
        grid.add(quitButton, 0, 5, 2, 1);
        grid.add(feedbackLabel, 0, 6, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        setScene(scene, "Register");
    }


    private boolean deleteSongFromDatabase(String songTitle) {
        String sql = "DELETE FROM songs WHERE name = ?";

        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, songTitle);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Song deleted successfully.");
                return true;
            } else {
                System.out.println("Song not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private boolean updateArtistNameInDatabase(String oldName, String newName) {
        String sql = "UPDATE artists SET name = ? WHERE name = ?";

        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setString(2, oldName);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Artist name updated successfully.");
                return true;
            } else {
                System.out.println("Artist not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private boolean addSongsToDatabase(String artistName, List<String> songs) {
        String fetchArtistIdQuery = "SELECT id FROM artists WHERE name = ?";
        String insertSongQuery = "INSERT INTO songs (name, artist_id, date) VALUES (?, ?, NOW())";

        try (Connection conn = connectDatabase()) {
            PreparedStatement fetchStmt = conn.prepareStatement(fetchArtistIdQuery);
            fetchStmt.setString(1, artistName);
            ResultSet rs = fetchStmt.executeQuery();

            if (rs.next()) {
                int artistId = rs.getInt("id");
                for (String song : songs) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSongQuery)) {
                        insertStmt.setString(1, song);
                        insertStmt.setInt(2, artistId);
                        insertStmt.executeUpdate();
                    }
                }
                System.out.println("Songs added successfully!");
                return true;
            } else {
                System.out.println("Artist not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private void showWelcomeScene() {
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label welcomeLabel = new Label("Welcome to Rhythm Revolutions!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        vbox.getChildren().add(welcomeLabel);

        Button accountInfoButton = new Button("Account Info");
        accountInfoButton.setStyle("-fx-background-color: green; fx-text-fill: white;");
        accountInfoButton.setOnAction(e -> showAccountInfoScene());

        Button exploreButton = new Button("Explore");
        exploreButton.setStyle("-fx-background-color: #8181ce; fx-text-fill: white;");
        exploreButton.setOnAction(e -> showExploreScene());

        Button quitButton = new Button("Exit");
        quitButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        quitButton.setOnAction(e -> showRoleSelectionScene()); // Redirect to role selection

        HBox buttonBox = new HBox(10, accountInfoButton, exploreButton, quitButton);
        buttonBox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(buttonBox);

        Scene scene = new Scene(vbox, 400, 300);
        stage.setTitle("Welcome");
        stage.setScene(scene);
        stage.show();
    }




    private void showLoginScene() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button loginButton = new Button("Log In");
        loginButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        Label feedbackLabel = new Label();

        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (isValidCredentials(email, password)) {
                showWelcomeScene();
            } else {
                feedbackLabel.setText("Invalid email or password.");
            }
        });

        Button switchToRegisterButton = new Button("Switch to Register");
        switchToRegisterButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");
        switchToRegisterButton.setOnAction(e -> showRegistrationScene());

        grid.add(new Label("Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 0, 2);
        grid.add(switchToRegisterButton, 1, 2);
        grid.add(feedbackLabel, 0, 3, 2, 1);

        Scene scene = new Scene(grid, 400, 300);
        stage.setTitle("Log In");
        setScene(scene, "Scene Title");
        stage.show();
    }

    private void addArtist(String artistName) {
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            String sql = "INSERT INTO followed_artists (artist_name, user_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, artistName);
            stmt.setInt(2, currentUser.getId());  // Assuming you have a getId() method in User
            stmt.executeUpdate();
            System.out.println("Artist added to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int addArtistToDatabase(String artistName) {
        String checkQuery = "SELECT id FROM artists WHERE name = ?";
        String insertQuery = "INSERT INTO artists (name, date) VALUES (?, NOW())";

        try (Connection conn = DatabaseConnection.connectDatabase();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            checkStmt.setString(1, artistName);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("Artist '" + artistName + "' already exists in the database.");
                return -1;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, artistName);
                insertStmt.executeUpdate();

                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int artistId = generatedKeys.getInt(1);
                    System.out.println("Artist '" + artistName + "' added with ID: " + artistId);
                    return artistId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }



    private void addSongToDatabase(String songTitle, int artistId) {
        String query = "INSERT INTO songs (name, artist_id, date) VALUES (?, ?, NOW())";
        try (Connection conn = DatabaseConnection.connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, songTitle);
            stmt.setInt(2, artistId);
            stmt.executeUpdate();
            System.out.println("Song '" + songTitle + "' added for artist ID: " + artistId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void showAddArtistScene() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField artistNameField = new TextField();
        artistNameField.setPromptText("Artist Name");

        VBox songsContainer = new VBox(10);
        TextField firstSongField = new TextField();
        firstSongField.setPromptText("Enter Song Title");
        songsContainer.getChildren().add(firstSongField);

        Button addSongButton = new Button("+ Add Song");
        addSongButton.setOnAction(e -> {
            TextField newSongField = new TextField();
            newSongField.setPromptText("Enter Song Title");
            songsContainer.getChildren().add(newSongField);
        });

        Button addButton = new Button("Add Artist and Songs");
        addButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        Label feedbackLabel = new Label();

        addButton.setOnAction(e -> {
            String artistName = artistNameField.getText().trim();
            List<String> songs = new ArrayList<>();

            for (javafx.scene.Node node : songsContainer.getChildren()) {
                if (node instanceof TextField songField) {
                    String songTitle = songField.getText().trim();
                    if (!songTitle.isEmpty()) {
                        songs.add(songTitle);
                    }
                }
            }

            if (artistName.isEmpty() || songs.isEmpty()) {
                feedbackLabel.setText("Error: Artist name and at least one song are required.");
                return;
            }

            int artistId = addArtistToDatabase(artistName);
            if (artistId != -1) {
                for (String song : songs) {
                    addSongToDatabase(song, artistId);
                }
                feedbackLabel.setText("Artist and Songs successfully added to the database!");
            } else {
                feedbackLabel.setText("Artist '" + artistName + "' already exists in the database.");
            }
        });


        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showAdminDashboard());

        grid.add(new Label("Artist Name:"), 0, 0);
        grid.add(artistNameField, 1, 0);
        grid.add(new Label("Songs:"), 0, 1);
        grid.add(songsContainer, 1, 1);
        grid.add(addSongButton, 1, 2);
        grid.add(addButton, 1, 3);
        grid.add(feedbackLabel, 1, 4);
        grid.add(backButton, 1, 5);

        Scene scene = new Scene(grid, 500, 400);
        setScene(scene, "Add Artist");
    }



    private void showUpdateArtistScene() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("Update Artist");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField artistNameField = new TextField();
        artistNameField.setPromptText("Enter artist's name");

        TextField newNameField = new TextField();
        newNameField.setPromptText("New name (optional)");

        Label feedbackLabel = new Label();

        Button updateNameButton = new Button("Update Name");
        updateNameButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");
        updateNameButton.setOnAction(e -> {
            String oldName = artistNameField.getText().trim();
            String newName = newNameField.getText().trim();

            if (!oldName.isEmpty() && !newName.isEmpty()) {
                boolean success = updateArtistNameInDatabase(oldName, newName);
                feedbackLabel.setText(success ? "Artist name updated!" : "Artist not found.");
            } else {
                feedbackLabel.setText("Please provide both current and new names.");
            }
        });

        Button addSongsButton = new Button("Add Songs");
        addSongsButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");
        addSongsButton.setOnAction(e -> {
            String artistName = artistNameField.getText().trim();
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Songs");
            dialog.setHeaderText("Enter songs (comma separated)");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(songInput -> {
                List<String> songs = Arrays.stream(songInput.split(","))
                        .map(String::trim)
                        .toList();
                boolean success = addSongsToDatabase(artistName, songs);
                feedbackLabel.setText(success ? "Songs added successfully!" : "Artist not found.");
            });
        });

        Button deleteSongButton = new Button("Delete Songs");
        deleteSongButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");
        deleteSongButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Delete Song");
            dialog.setHeaderText("Enter the song title to delete");
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(songTitle -> {
                boolean success = deleteSongFromDatabase(songTitle);
                feedbackLabel.setText(success ? "Song deleted successfully!" : "Song not found.");
            });
        });

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showAdminDashboard());

        HBox buttonBox = new HBox(10, updateNameButton, addSongsButton, deleteSongButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        layout.getChildren().addAll(
                titleLabel,
                new Label("Artist Name:"), artistNameField,
                new Label("New Name (optional):"), newNameField,
                buttonBox,
                feedbackLabel,
                backButton
        );

        Scene scene = new Scene(layout, 500, 400);
        setScene(scene, "Update Artist");
    }



    private Artist findArtistByName(String artistName) {
        try (Scanner scanner = new Scanner(new File(ARTISTS_FILE))) {
            while (scanner.hasNextLine()) {
                Artist artist = Artist.fromFileString(scanner.nextLine());
                if (artist != null && artist.getName().equalsIgnoreCase(artistName)) {
                    return artist;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void saveArtist(Artist artist) {
        File inputFile = new File(ARTISTS_FILE);

        if (!inputFile.exists()) {
            System.out.println("Artist file does not exist.");
            return;
        }

        List<String> updatedLines = new ArrayList<>();
        boolean artistFound = false;

        try (Scanner scanner = new Scanner(inputFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Artist existingArtist = Artist.fromFileString(line);

                if (existingArtist != null && existingArtist.getName().equalsIgnoreCase(artist.getName())) {

                    updatedLines.add(artist.toFileString());
                    artistFound = true;
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!artistFound) {
            System.out.println("Artist not found. No changes made.");
            return;
        }


        try (FileWriter writer = new FileWriter(inputFile, false)) { // Overwrite the file
            for (String line : updatedLines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setScene(Scene scene, String title) {
        stage.setTitle(title);
        stage.setScene(scene);

        scene.widthProperty().addListener((obs, oldVal, newVal) -> scaleElements(scene));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> scaleElements(scene));
        stage.show();
    }

    private boolean deleteArtistFromDatabase(String artistName) {
        String sql = "DELETE FROM artists WHERE name = ?";

        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artistName);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Artist deleted successfully.");
                return true;
            } else {
                System.out.println("Artist not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private boolean deleteUserFromDatabase(String userEmail) {
        String sql = "DELETE FROM users WHERE email = ?";

        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userEmail);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User deleted successfully.");
                return true;
            } else {
                System.out.println("User not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private void scaleElements(Scene scene) {
        double width = scene.getWidth();
        double height = scene.getHeight();

        // Dynamically adjust font sizes and padding
        scene.getRoot().getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof VBox vbox) {
                vbox.setSpacing(height * 0.02); // Adjust spacing dynamically
                vbox.getChildren().forEach(child -> {
                    if (child instanceof Label label) {
                        label.setStyle("-fx-font-size: " + (width * 0.03) + "px;");
                    } else if (child instanceof Button button) {
                        button.setPrefWidth(width * 0.2); // 20% of the scene width
                        button.setPrefHeight(height * 0.1); // 10% of the scene height
                        button.setStyle("-fx-font-size: " + (width * 0.02) + "px;");
                    }
                });
            } else if (node instanceof GridPane gridPane) {
                gridPane.setHgap(width * 0.02); // Adjust horizontal gap
                gridPane.setVgap(height * 0.02); // Adjust vertical gap
                gridPane.getChildren().forEach(child -> {
                    if (child instanceof TextField || child instanceof PasswordField) {
                        ((TextField) child).setPrefWidth(width * 0.3); // 30% of scene width
                    } else if (child instanceof Button button) {
                        button.setPrefWidth(width * 0.2); // 20% of scene width
                        button.setPrefHeight(height * 0.1); // 10% of scene height
                    }
                });
            }
        });
    }


    private void showDeleteUserScene() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter user email to delete");

        Button deleteButton = new Button("Delete User");
        deleteButton.setStyle("-fx-background-color: #797cb5; -fx-text-fill: white;");
        Label feedbackLabel = new Label();

        deleteButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (deleteUserFromDatabase(email)) {
                feedbackLabel.setText("User deleted successfully!");
            } else {
                feedbackLabel.setText("User not found.");
            }
        });





        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showAdminDashboard());

        grid.add(new Label("User Email:"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(deleteButton, 0, 1);
        grid.add(backButton, 1, 1);
        grid.add(feedbackLabel, 0, 2, 2, 1);

        Scene scene = new Scene(grid, 400, 200);
        setScene(scene, "Delete User");
    }

    public boolean updateUserPassword(String email, String newPassword) {
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            String sql = "UPDATE users SET password = ? WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private boolean deleteUser(String email) {
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            String sql = "DELETE FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("User deleted successfully.");
                return true;
            } else {
                System.out.println("User not found.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    private void showDeleteArtistScene() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField artistNameField = new TextField();
        artistNameField.setPromptText("Enter artist name to delete");

        Button deleteButton = new Button("Delete Artist");
        deleteButton.setStyle("-fx-background-color: #797cb5; -fx-text-fill: white;");

        Label feedbackLabel = new Label();

        deleteButton.setOnAction(e -> {
            String artistName = artistNameField.getText().trim();
            if (deleteArtistFromDatabase(artistName)) {
                feedbackLabel.setText("Artist deleted successfully!");
            } else {
                feedbackLabel.setText("Artist not found.");
            }
        });


        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        backButton.setOnAction(e -> showAdminDashboard());

        grid.add(new Label("Artist Name:"), 0, 0);
        grid.add(artistNameField, 1, 0);
        grid.add(deleteButton, 0, 1);
        grid.add(backButton, 1, 1);
        grid.add(feedbackLabel, 0, 2, 2, 1);

        Scene scene = new Scene(grid, 400, 200);
        setScene(scene, "Delete Artist");
    }


    private boolean deleteArtist(String artistName) {
        File inputFile = new File(ARTISTS_FILE);

        if (!inputFile.exists()) {
            System.out.println("Artist file does not exist.");
            return false;
        }

        List<String> updatedLines = new ArrayList<>();
        boolean artistFound = false;

        try (Scanner scanner = new Scanner(inputFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Artist existingArtist = Artist.fromFileString(line);

                if (existingArtist != null && existingArtist.getName().equalsIgnoreCase(artistName)) {
                    artistFound = true; // Mark as found and skip this artist
                } else {
                    updatedLines.add(line); // Keep other lines
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write back all remaining lines
        try (FileWriter writer = new FileWriter(inputFile, false)) {
            for (String line : updatedLines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artistFound;
    }


    private void showAllUsersScene() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("All Registered Users");
        ListView<String> userListView = new ListView<>();

        try (Connection conn = connectDatabase()) {
            String query = "SELECT id, name, email, date FROM users";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String date = rs.getString("date");
                userListView.getItems().add("ID: " + id + ", Name: " + name + ", Email: " + email + ", Date: " + date);
            }
        } catch (SQLException e) {
            userListView.getItems().add("Error retrieving users.");
            e.printStackTrace();
        }

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        backButton.setOnAction(e -> showAdminDashboard());

        vbox.getChildren().addAll(titleLabel, userListView, backButton);
        setScene(new Scene(vbox, 500, 400), "All Users");
    }



    private void showAdminLoginScene() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Admin Login");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Password input field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Admin Password");

        // Buttons
        Button loginButton = new Button("Log In");
        loginButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");


        // Feedback label
        Label feedbackLabel = new Label();

        // Button Actions
        loginButton.setOnAction(e -> {
            String password = passwordField.getText().trim();
            if (password.equals(ADMIN_PASSWORD)) {
                feedbackLabel.setText("Login successful!");
                showAdminDashboard();
            } else {
                feedbackLabel.setText("Invalid password. Try again.");
            }
        });

        backButton.setOnAction(e -> showRoleSelectionScene());



        // Layout setup
        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 0, 2);
        grid.add(backButton, 1, 2);
        grid.add(feedbackLabel, 0, 4, 2, 1);

        // Create and set the scene
        Scene scene = new Scene(grid, 400, 300);
        setScene(scene, "Admin Login");
    }



    private void showAllArtistsScene() {
        VBox vbox = new VBox(10);
        Label titleLabel = new Label("All Registered Artists");
        ListView<String> artistListView = new ListView<>();

        try (Connection conn = connectDatabase()) {
            String query = "SELECT id, name, date FROM artists";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String date = rs.getString("date");
                artistListView.getItems().add("ID: " + id + ", Name: " + name + ", Date Added: " + date);
            }
        } catch (SQLException e) {
            artistListView.getItems().add("Error retrieving artists from the database.");
            e.printStackTrace();
        }

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showAdminDashboard());

        vbox.getChildren().addAll(titleLabel, artistListView, backButton);
        setScene(new Scene(vbox, 500, 400), "All Artists");
    }



    private void showAdminDashboard() {
        VBox vbox = new VBox(15); // Vertical layout with spacing
        vbox.setAlignment(Pos.CENTER); // Center align all elements

        // Title Label
        Label titleLabel = new Label("Welcome, Admin!");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;"); // Styling the title

        // Buttons
        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");

        Button addArtistButton = new Button("Add Artist");
        addArtistButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");

        Button deleteArtistButton = new Button("Delete Artist");
        deleteArtistButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");

        Button updateArtistButton = new Button("Update Artist");
        updateArtistButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");

        Button seeUsersButton = new Button("See Users");
        seeUsersButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");

        Button seeArtistsButton = new Button("See Artists");
        seeArtistsButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");

        Button quitButton = new Button("Quit");
        quitButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        // Button Actions
        deleteUserButton.setOnAction(e -> showDeleteUserScene());
        addArtistButton.setOnAction(e -> showAddArtistScene());
        deleteArtistButton.setOnAction(e -> showDeleteArtistScene());
        updateArtistButton.setOnAction(e -> showUpdateArtistScene());
        seeUsersButton.setOnAction(e -> showAllUsersScene());
        seeArtistsButton.setOnAction(e -> showAllArtistsScene());
        quitButton.setOnAction(e -> showRoleSelectionScene());

        // Add all elements to VBox
        vbox.getChildren().addAll(
                titleLabel,             // Add the title at the top
                deleteUserButton,
                addArtistButton,
                deleteArtistButton,
                updateArtistButton,
                seeUsersButton,
                seeArtistsButton,
                quitButton
        );

        // Create and set the scene
        Scene scene = new Scene(vbox, 400, 400);
        setScene(scene, "Admin Dashboard");
    }

    // Helper Method to Get Followed Artists
    public List<String> getFollowedArtists() {
        List<String> artists = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            String sql = "SELECT artist_name FROM followed_artists WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                artists.add(rs.getString("artist_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artists;
    }


    public void saveUserToDatabase(String name, String password, String email) {
        String query = "INSERT INTO users (name, password, email, account_created) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.executeUpdate();
            System.out.println("User successfully added to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper Method to Get User Playlists
    public List<String> getUserPlaylists() {
        List<String> playlists = new ArrayList<>();
        try (Connection conn = DatabaseConnection.connectDatabase()) {
            String sql = "SELECT name FROM playlists WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }


    private List<String> getFollowedArtistsFromDatabase(int userId) {
        List<String> artists = new ArrayList<>();
        String sql = "SELECT a.name FROM followed_artists fa JOIN artists a ON fa.artist_id = a.id WHERE fa.user_id = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                artists.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artists;
    }



    private List<String> getUserPlaylistsFromDatabase(int userId) {
        List<String> playlists = new ArrayList<>();
        String sql = "SELECT p.name FROM playlists p WHERE p.user_id = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                playlists.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playlists;
    }


    private boolean deleteSongFromPlaylist(String playlistName, String songName) {
        String sql = """
        DELETE ps 
        FROM playlist_songs ps 
        JOIN songs s ON ps.song_id = s.id 
        JOIN playlists p ON ps.playlist_id = p.id 
        WHERE s.name = ? AND p.name = ? AND p.user_id = ?;
    """;
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, songName);
            stmt.setString(2, playlistName);
            stmt.setInt(3, currentUser.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addSongToPlaylistByName(String playlistName, String songName) {
        int playlistId = getPlaylistIdByName(playlistName);
        int songId = getSongIdByName(songName);

        if (playlistId != -1 && songId != -1) {
            String sql = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)";
            try (Connection conn = connectDatabase();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, playlistId);
                stmt.setInt(2, songId);
                stmt.executeUpdate();
                System.out.println("Song added successfully to playlist: " + playlistName);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: Playlist or Song not found.");
        }
    }


    private void showAddSongToPlaylistScreen(int playlistId, String playlistName) {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Add Songs to Playlist: " + playlistName);
        titleLabel.setStyle("-fx-font-size: 18px;");

        ListView<String> songListView = new ListView<>();
        songListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Fetch songs that are not already in the playlist
        try (Connection conn = connectDatabase()) {
            String sql = """
            SELECT name FROM songs 
            WHERE id NOT IN (
                SELECT song_id FROM playlist_songs WHERE playlist_id = ?
            )
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                songListView.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add Selected Songs Button
        Button addButton = new Button("Add Selected Songs");
        addButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        addButton.setOnAction(e -> {
            ObservableList<String> selectedSongs = songListView.getSelectionModel().getSelectedItems();
            if (!selectedSongs.isEmpty()) {
                for (String song : selectedSongs) {
                    int songId = getSongIdByName(song);
                    if (songId != -1) {
                        addSongToPlaylist(playlistId, songId);
                    }
                }
                showEditPlaylistScreen(playlistId, playlistName);  // Refresh the playlist screen
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one song.");
                alert.showAndWait();
            }
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showEditPlaylistScreen(playlistId, playlistName));

        vbox.getChildren().addAll(titleLabel, songListView, addButton, backButton);
        setScene(new Scene(vbox, 500, 500), "Add Songs to Playlist");
    }

    private void showAccountInfoScene() {
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No user is currently logged in.");
            alert.showAndWait();
            return;
        }

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("Account Information");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label nameLabel = new Label("Name: " + currentUser.getName());
        Label emailLabel = new Label("Email: " + currentUser.getEmail());
        Label accountCreatedLabel = new Label("Created on date: " + currentUser.getFormattedAccountCreated());

        // Followed Artists Button
        Button followedArtistsButton = new Button("Followed Artists");
        followedArtistsButton.setStyle("-fx-background-color: #8181ce; fx-text-fill: white;");
        followedArtistsButton.setOnAction(e -> showFollowedArtistsScreen());

        // Playlists Button
        Button playlistsButton = new Button("Playlists");
        playlistsButton.setStyle("-fx-background-color: #8181ce; fx-text-fill: white;");
        playlistsButton.setOnAction(e -> showPlaylistsScreen());

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showWelcomeScene());

        vbox.getChildren().addAll(
                titleLabel, nameLabel, emailLabel, accountCreatedLabel,
                followedArtistsButton, playlistsButton, backButton
        );

        Scene scene = new Scene(vbox, 500, 400);
        setScene(scene, "Account Information");
    }

    private boolean unfollowArtist(String artistName) {
        String sql = """
        DELETE fa FROM followed_artists fa 
        JOIN artists a ON fa.artist_id = a.id 
        WHERE a.name = ? AND fa.user_id = ?;
    """;

        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artistName);
            stmt.setInt(2, currentUser.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Artist unfollowed successfully.");
                return true;
            } else {
                System.out.println("Artist not found or already unfollowed.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Display Followed Artists Scene

    private void showFollowedArtistsScreen() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("Followed Artists");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> artistListView = new ListView<>();  // List to hold followed artists
        artistListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Allow single selection

        // Load followed artists from the database
        try (Connection conn = connectDatabase()) {
            String sql = """
            SELECT a.name 
            FROM followed_artists fa 
            JOIN artists a ON fa.artist_id = a.id 
            WHERE fa.user_id = ?;
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                artistListView.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            artistListView.getItems().add("Error loading followed artists.");
            e.printStackTrace();
        }

        // Button to unfollow the selected artist
        Button unfollowButton = new Button("Unfollow Artist");
        unfollowButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");

        Label feedbackLabel = new Label();

        // When the button is clicked, remove the artist
        unfollowButton.setOnAction(e -> {
            String selectedArtist = artistListView.getSelectionModel().getSelectedItem();
            if (selectedArtist != null) {
                boolean success = unfollowArtist(selectedArtist);
                if (success) {
                    artistListView.getItems().remove(selectedArtist); // Remove from the list view
                    feedbackLabel.setText("Artist unfollowed successfully!");
                } else {
                    feedbackLabel.setText("Failed to unfollow artist.");
                }
            } else {
                feedbackLabel.setText("Please select an artist to unfollow.");
            }
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showAccountInfoScene());

        vbox.getChildren().addAll(titleLabel, artistListView, unfollowButton, feedbackLabel, backButton);
        setScene(new Scene(vbox, 500, 400), "Followed Artists");
    }


    private boolean deletePlaylistFromDatabase(String playlistName) {
        String sql = "DELETE FROM playlists WHERE name = ? AND user_id = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playlistName);
            stmt.setInt(2, currentUser.getId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void removeSongFromPlaylist(int playlistId, int songId) {
        try (Connection conn = connectDatabase()) {
            String sql = "DELETE FROM playlist_songs WHERE playlist_id = ? AND song_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.executeUpdate();
            System.out.println("Song removed successfully from the playlist.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showEditPlaylistScreen(int playlistId, String playlistName) {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("Edit Playlist: " + playlistName);
        titleLabel.setStyle("-fx-font-size: 18px;");

        ListView<String> songListView = new ListView<>();
        Map<String, String> songPathMap = new HashMap<>(); // Store song names and their file paths

        // Load songs for the playlist
        try (Connection conn = connectDatabase()) {
            String sql = """
        SELECT s.name, s.file_path 
        FROM songs s 
        JOIN playlist_songs ps ON s.id = ps.song_id 
        WHERE ps.playlist_id = ?;
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String songName = rs.getString("name");
                String filePath = rs.getString("file_path");
                songListView.getItems().add(songName); // Only show the name
                songPathMap.put(songName, filePath);   // Store the path for internal use
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Playback Buttons
        Button playButton = new Button("Play");
        playButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        playButton.setOnAction(e -> {
            String selectedSong = songListView.getSelectionModel().getSelectedItem();
            if (selectedSong != null && songPathMap.containsKey(selectedSong)) {
                String filePath = songPathMap.get(selectedSong);
                File songFile = new File(filePath);
                if (songFile.exists()) {
                    playSong(songFile);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "File not found: " + filePath);
                    alert.showAndWait();
                }
            }
        });

        Button pauseButton = new Button("Pause");
        pauseButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        pauseButton.setOnAction(e -> pauseSong());

        Button stopButton = new Button("Stop");
        stopButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        stopButton.setOnAction(e -> stopSong());

        HBox playbackControls = new HBox(10, playButton, pauseButton, stopButton);

        // Delete Song Button
        Button deleteSongButton = new Button("Delete Song");
        deleteSongButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deleteSongButton.setOnAction(e -> {
            String selectedSong = songListView.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                int songId = getSongIdByName(selectedSong);
                removeSongFromPlaylist(playlistId, songId);
                songListView.getItems().remove(selectedSong); // Remove from ListView
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Song deleted from playlist.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a song to delete.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showPlaylistsScreen());

        vbox.getChildren().addAll(titleLabel, songListView, playbackControls, deleteSongButton, backButton);

        Scene scene = new Scene(vbox, 600, 500);
        setScene(scene, "Edit Playlist");
    }







    private void deletePlaylistById(int playlistId) {
        try (Connection conn = connectDatabase()) {
            String sql = "DELETE FROM playlists WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, playlistId);
            stmt.executeUpdate();
            System.out.println("Playlist deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startPlaylistPlayback(int playlistId) {
        try (Connection conn = connectDatabase()) {
            String sql = """
            SELECT s.file_path 
            FROM songs s 
            JOIN playlist_songs ps ON s.id = ps.song_id 
            WHERE ps.playlist_id = ?;
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String filePath = rs.getString("file_path");
                if (filePath == null || filePath.isEmpty()) {
                    System.out.println("Error: No valid file path found for this song.");
                    return;
                }

                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("Error: The file does not exist at path: " + filePath);
                    return;
                }

                Media media = new Media(file.toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
                System.out.println("🎵 Now Playing: " + filePath);
            } else {
                System.out.println("No songs found for the selected playlist.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ** Method to Play a Single Song **
    private void playMusic(File songFile) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();  // Stop previous song if playing
            }

            Media media = new Media(songFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

            // Update time label
            mediaPlayer.currentTimeProperty().addListener((observable) -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                songTimeLabel.setText(formatTime(currentTime));
            });

            mediaPlayer.setOnEndOfMedia(this::stopMusic);

            System.out.println("🎵 Now Playing: " + songFile.getName());

        } catch (Exception e) {
            System.err.println("Error playing the song: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playMusicFromDatabase(int songId) {
        try (Connection conn = connectDatabase()) {
            String sql = "SELECT file_path FROM songs WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, songId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String filePath = rs.getString("file_path");
                System.out.println("File Path Retrieved: " + filePath);  // Debugging line
                File songFile = new File(filePath);

                if (songFile.exists()) {
                    playMusic(songFile);
                } else {
                    System.err.println("File not found at: " + filePath);  // Debugging line
                    Alert alert = new Alert(Alert.AlertType.ERROR, "File not found: " + filePath);
                    alert.showAndWait();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            System.out.println("⏸️ Song paused.");
        }
    }

    // Pause the current song
    private void pauseSong() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.pause();
            currentSongLabel.setText("Paused: " + currentSongLabel.getText().replace("Playing: ", ""));
        }
    }

    // Stop the current song
    private void stopSong() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.stop();
            currentSongLabel.setText("Stopped");
        }
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            System.out.println("⏹️ Song stopped.");
        }
    }

    // ** Next Song Method **
    private void playNextSong() {
        if (currentSongIndex < playlistSongs.size() - 1) {
            currentSongIndex++;
            playMusic(playlistSongs.get(currentSongIndex));
        } else {
            System.out.println("End of playlist.");
        }
    }

    // ** Previous Song Method **
    private void playPreviousSong() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
            playMusic(playlistSongs.get(currentSongIndex));
        }
    }

    // ** Stop Playback **

    // ** Format Time Method **
    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void loadPlaylistSongs(int playlistId) {
        playlistSongs.clear();
        String sql = """
        SELECT s.file_path 
        FROM songs s 
        JOIN playlist_songs ps ON s.id = ps.song_id 
        WHERE ps.playlist_id = ?;
    """;
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String filePath = rs.getString("file_path");
                playlistSongs.add(new File(filePath));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Display Playlists Screen
    private void showPlaylistsScreen() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("Playlists");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> playlistListView = new ListView<>();
        Map<String, Integer> playlistMap = new HashMap<>(); // Store playlist names with IDs

        // Load playlists
        try (Connection conn = connectDatabase()) {
            String sql = "SELECT id, name FROM playlists WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                playlistListView.getItems().add(name);
                playlistMap.put(name, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button deletePlaylistButton = new Button("Delete Playlist");
        deletePlaylistButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        deletePlaylistButton.setOnAction(e -> {
            String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null) {
                int playlistId = playlistMap.get(selectedPlaylist);
                deletePlaylistById(playlistId);
                playlistListView.getItems().remove(selectedPlaylist);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Playlist deleted successfully.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a playlist to delete.");
                alert.showAndWait();
            }
        });

        Button editPlaylistButton = new Button("Edit Playlist");
        editPlaylistButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
        editPlaylistButton.setOnAction(e -> {
            String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null) {
                int playlistId = playlistMap.get(selectedPlaylist);
                showEditPlaylistScreen(playlistId, selectedPlaylist);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a playlist to edit.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showWelcomeScene());

        HBox buttonBox = new HBox(10, editPlaylistButton, deletePlaylistButton, backButton);
        vbox.getChildren().addAll(titleLabel, playlistListView, buttonBox);

        Scene scene = new Scene(vbox, 500, 400);
        setScene(scene, "Playlists");
    }


    private void playSong(File songFile) {
        try {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.stop();
            }
            Media media = new Media(songFile.toURI().toString());
            currentMediaPlayer = new MediaPlayer(media);
            currentMediaPlayer.play();

            currentSongLabel.setText("Playing: " + songFile.getName());

            /*currentMediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                System.out.println("Current Time: " + newTime.toMinutes() + " min");
            });*/

            currentMediaPlayer.setOnEndOfMedia(() -> {
                currentSongLabel.setText("Song finished: " + songFile.getName());
            });
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Unable to play song.");
            alert.showAndWait();
        }
    }


    public void followArtist(String artistName) {
        if (currentUser == null) {
            System.out.println("No user is logged in.");
            return;
        }

        String sql = "UPDATE users SET followed_artists = CONCAT(IFNULL(followed_artists, ''), ?) WHERE email = ?";

        try (Connection conn = DatabaseConnection.connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, artistName + ";");
            stmt.setString(2, currentUser.getEmail());
            stmt.executeUpdate();
            System.out.println("Artist followed successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isPlaylistNameUnique(String playlistName) {
        String sql = "SELECT COUNT(*) FROM playlists WHERE name = ? AND user_id = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playlistName);
            stmt.setInt(2, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false;  // Playlist already exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }



    private void showCustomPlaylistCreation() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Create a Custom Playlist");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField playlistNameField = new TextField();
        playlistNameField.setPromptText("Enter Playlist Name");

        TextField searchField = new TextField();
        searchField.setPromptText("Search for songs by Artist or Song Name");

        ListView<String> searchResultsView = new ListView<>();
        searchResultsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        ListView<String> selectedSongsView = new ListView<>();
        ObservableList<String> selectedSongs = javafx.collections.FXCollections.observableArrayList();
        selectedSongsView.setItems(selectedSongs);

        Button searchButton = new Button("Search Songs");
        searchButton.setStyle("-fx-background-color: purple; -fx-text-fill: white;");
        searchButton.setOnAction(e -> {
            String searchQuery = searchField.getText().trim();
            searchResultsView.getItems().clear();

            try (Connection conn = DatabaseConnection.connectDatabase()) {
                String sql = """
                SELECT DISTINCT s.name
                FROM songs s
                LEFT JOIN artists a ON s.artist_id = a.id
                WHERE s.name LIKE ? OR a.name LIKE ?
                ORDER BY s.name
            """;
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, "%" + searchQuery + "%");
                stmt.setString(2, "%" + searchQuery + "%");

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    searchResultsView.getItems().add(rs.getString("name"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        Button addSelectedButton = new Button("Add Selected Songs");
        addSelectedButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");
        addSelectedButton.setOnAction(e -> {
            ObservableList<String> picked = searchResultsView.getSelectionModel().getSelectedItems();
            for (String song : picked) {
                if (!selectedSongs.contains(song)) {
                    selectedSongs.add(song);
                }
            }
        });

        Button removeSelectedButton = new Button("Remove Selected");
        removeSelectedButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        removeSelectedButton.setOnAction(e -> {
            String selected = selectedSongsView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedSongs.remove(selected);
            }
        });

        Button createButton = new Button("Save to Playlist");
        createButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        createButton.setOnAction(e -> {
            String playlistName = playlistNameField.getText().trim();

            if (playlistName.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please provide a playlist name.");
                alert.showAndWait();
                return;
            }

            if (selectedSongs.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please select at least one song.");
                alert.showAndWait();
                return;
            }

            if (isPlaylistNameUnique(playlistName)) {
                createPlaylist(playlistName, new ArrayList<>(selectedSongs));
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Playlist created successfully!");
                alert.showAndWait();
                showPlaylistsScreen();
            } else {
                int playlistId = getPlaylistIdByName(playlistName);
                for (String songName : selectedSongs) {
                    int songId = getSongIdByName(songName);
                    addSongToPlaylist(playlistId, songId);
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Songs added to the existing playlist!");
                alert.showAndWait();
                showPlaylistsScreen();
            }
        });

        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showCreatePlaylistScene());

        Label searchResultsLabel = new Label("Search Results");
        Label selectedSongsLabel = new Label("Selected Songs");

        vbox.getChildren().addAll(
                titleLabel,
                playlistNameField,
                searchField,
                searchButton,
                searchResultsLabel,
                searchResultsView,
                addSelectedButton,
                selectedSongsLabel,
                selectedSongsView,
                removeSelectedButton,
                createButton,
                backButton
        );

        setScene(new Scene(vbox, 600, 700), "Create Custom Playlist");
    }

    private void createSuggestedPlaylist() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        // 🎨 Updated Title with New Wording
        Label titleLabel = new Label("Based on artists you follow:");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ListView<String> suggestedSongList = new ListView<>();

        try (Connection conn = connectDatabase()) {
            String sql = """
            SELECT DISTINCT s.name 
            FROM songs s 
            JOIN artists a ON s.artist_id = a.id 
            JOIN followed_artists fa ON fa.artist_id = a.id 
            WHERE fa.user_id = ?;
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suggestedSongList.getItems().add(rs.getString("name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 🎨 Save Suggested Playlist Button (Green)
        Button saveSuggestedButton = new Button("Save Suggested Playlist");
        saveSuggestedButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");

        // ✅ Save Button Action with Pop-up for Naming
        saveSuggestedButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Name Your Playlist");
            dialog.setHeaderText("Name of this playlist:");
            dialog.setContentText("Playlist Name:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(playlistName -> {
                if (!playlistName.trim().isEmpty()) {
                    if (isPlaylistNameUnique(playlistName)) {  // ✅ Prevent Duplicates
                        createPlaylist(playlistName, suggestedSongList.getItems());
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Playlist saved successfully!");
                        successAlert.showAndWait();
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "A playlist with this name already exists.");
                        errorAlert.showAndWait();
                    }
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Playlist name cannot be empty.");
                    errorAlert.showAndWait();
                }
            });
        });

        // 🎨 Back Button (Red with White Text)
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showCreatePlaylistScene());

        // Adding elements to the VBox
        vbox.getChildren().addAll(titleLabel, suggestedSongList, saveSuggestedButton, backButton);
        setScene(new Scene(vbox, 500, 500), "Suggested Playlist");
    }


    private void showCreatePlaylistScene() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Create Playlist Options");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Button for Custom Playlist Creation
        Button customPlaylistButton = new Button("Create Custom Playlist");
        customPlaylistButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        customPlaylistButton.setOnAction(e -> showCustomPlaylistCreation());

        // Button for Suggested Playlist Creation
        Button suggestedPlaylistButton = new Button("Suggested Playlist");
        suggestedPlaylistButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        suggestedPlaylistButton.setOnAction(e -> createSuggestedPlaylist());

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showExploreScene());

        vbox.getChildren().addAll(titleLabel, customPlaylistButton, suggestedPlaylistButton, backButton);

        Scene scene = new Scene(vbox, 500, 400);
        setScene(scene, "Playlist Options");
    }






    private void showSearchSongsScene(String playlistName) {
        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);

        Label searchLabel = new Label("Search Songs");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter Artist or Song Name");

        ListView<String> songResults = new ListView<>();
        Button searchButton = new Button("Search");

        // **Search Functionality (By Artist or Song)**
        searchButton.setOnAction(e -> {
            String searchTerm = searchField.getText().trim().toLowerCase();
            songResults.getItems().clear();

            try (Scanner scanner = new Scanner(new File(ARTISTS_FILE))) {
                while (scanner.hasNextLine()) {
                    String[] data = scanner.nextLine().split(",");
                    String artistName = data[0];
                    if (artistName.toLowerCase().contains(searchTerm)) {
                        for (int i = 1; i < data.length; i++) {
                            songResults.getItems().add(data[i]);
                        }
                    } else {
                        for (int i = 1; i < data.length; i++) {
                            if (data[i].toLowerCase().contains(searchTerm)) {
                                songResults.getItems().add(data[i]);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                songResults.getItems().add("Error loading songs.");
            }
        });

        Button addToPlaylistButton = new Button("Add to Playlist");
        addToPlaylistButton.setOnAction(e -> {
            String selectedSong = songResults.getSelectionModel().getSelectedItem();
            if (selectedSong != null && !playlistName.isEmpty()) {
                Playlist playlist = new Playlist(playlistName);
                playlist.addSong(new Song(selectedSong));
                currentUser.addPlaylist(playlist);
                saveUserData();  // Save to users.txt
                showWelcomeScene();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No song selected or playlist name empty.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> showCreatePlaylistScene());

        vbox.getChildren().addAll(searchLabel, searchField, searchButton, songResults, addToPlaylistButton, backButton);
        Scene scene = new Scene(vbox, 400, 400);
        stage.setScene(scene);
    }

    private void followArtistByName(String artistName) {
        try (Connection conn = connectDatabase()) {
            // Fetch the artist ID
            String getArtistIdQuery = "SELECT id FROM artists WHERE name = ?";
            PreparedStatement getArtistStmt = conn.prepareStatement(getArtistIdQuery);
            getArtistStmt.setString(1, artistName);
            ResultSet artistRs = getArtistStmt.executeQuery();

            if (artistRs.next()) {
                int artistId = artistRs.getInt("id");

                // Check if the user already follows the artist
                String checkFollowQuery = "SELECT * FROM followed_artists WHERE user_id = ? AND artist_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkFollowQuery);
                checkStmt.setInt(1, currentUser.getId());
                checkStmt.setInt(2, artistId);
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next()) {
                    System.out.println("You already follow " + artistName);
                } else {
                    // Add the artist to the followed list
                    String followQuery = "INSERT INTO followed_artists (user_id, artist_id, date) VALUES (?, ?, NOW())";
                    PreparedStatement followStmt = conn.prepareStatement(followQuery);
                    followStmt.setInt(1, currentUser.getId());
                    followStmt.setInt(2, artistId);
                    followStmt.executeUpdate();
                    System.out.println("Successfully followed " + artistName);
                }
            } else {
                System.out.println("Artist not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showFollowArtistScene() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label("Follow an Artist");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ListView for Artists
        ListView<String> artistListView = new ListView<>();
        artistListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Fetch Only Artists Not Yet Followed
        try (Connection conn = connectDatabase()) {
            String sql = """
            SELECT a.name 
            FROM artists a 
            WHERE a.id NOT IN (
                SELECT artist_id 
                FROM followed_artists 
                WHERE user_id = ?
            )
        """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                artistListView.getItems().add(rs.getString("name"));
            }
        } catch (SQLException e) {
            artistListView.getItems().add("Error loading artists.");
            e.printStackTrace();
        }

        // Follow Button Logic
        Button followButton = new Button("Follow");
        followButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        Label feedbackLabel = new Label();

        followButton.setOnAction(e -> {
            ObservableList<String> selectedArtists = artistListView.getSelectionModel().getSelectedItems();
            if (!selectedArtists.isEmpty()) {
                for (String artist : selectedArtists) {
                    followArtistByName(artist);
                }
                feedbackLabel.setText("You successfully followed the selected artists!");
                showFollowArtistScene();  // Refresh the list after following
            } else {
                feedbackLabel.setText("Please select at least one artist.");
            }
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showExploreScene());

        vbox.getChildren().addAll(titleLabel, artistListView, followButton, feedbackLabel, backButton);
        setScene(new Scene(vbox, 500, 400), "Follow Artist");
    }




    private void saveUserData() {
        try (FileWriter writer = new FileWriter("users.txt", false)) {
            writer.write(currentUser.toFileString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getSongId(String songTitle) {
        String query = "SELECT id FROM songs WHERE name = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, songTitle);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id"); // Return the song ID if found
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if the song is not found
    }

    private int getPlaylistIdByName(String playlistName) {
        String query = "SELECT id FROM playlists WHERE name = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playlistName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if the playlist doesn't exist
    }



    private int getSongIdByName(String songName) {
        String query = "SELECT id FROM songs WHERE name = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, songName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if the song doesn't exist
    }


    private String getPlaylistNameById(int playlistId) {
        String query = "SELECT name FROM playlists WHERE id = ?";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, playlistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void addSongToPlaylist(int playlistId, int songId) {
        String query = "INSERT INTO playlist_songs (playlist_id, song_id) VALUES (?, ?)"; // Fixed the query
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.executeUpdate();
            System.out.println("Song added to playlist successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createPlaylist(String playlistName, List<String> songNames) {
        String insertPlaylistQuery = "INSERT INTO playlists (name, user_id) VALUES (?, ?)";
        try (Connection conn = connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(insertPlaylistQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, playlistName);
            stmt.setInt(2, currentUser.getId());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int playlistId = rs.getInt(1);
                for (String songName : songNames) {
                    int songId = getSongIdByName(songName);
                    addSongToPlaylist(playlistId, songId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    private void showExploreScene() {
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label titleLabel = new Label("Explore Options");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // ✅ Follow Artist Button
        Button followArtistButton = new Button("Follow Artist");
        followArtistButton.setStyle("-fx-background-color: #8181ce; -fx-text-fill: white;");
        followArtistButton.setOnAction(e -> showFollowArtistScene());

        // ✅ Create Playlist Button
        Button createPlaylistButton = new Button("Create Playlist");
        createPlaylistButton.setStyle("-fx-background-color: #8181ce; fx-text-fill: white;");
        createPlaylistButton.setOnAction(e -> showCreatePlaylistScene());

        // ✅ Back Button (Red)
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        backButton.setOnAction(e -> showWelcomeScene());

        vbox.getChildren().addAll(titleLabel, followArtistButton, createPlaylistButton, backButton);

        Scene scene = new Scene(vbox, 500, 400);
        setScene(scene, "Explore Options");
    }



    private boolean isValidEmail(String email) {
        return EmailVerification.isEmailFormatValid(email); // Call the API method directly
    }



    public boolean isValidCredentials(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.connectDatabase();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentUser = new User(rs.getString("name"), rs.getString("password"), rs.getString("email"));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
