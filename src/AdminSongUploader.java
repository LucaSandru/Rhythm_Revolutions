import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class AdminSongUploader {
    private Stage stage;

    public AdminSongUploader(Stage primaryStage) {
        this.stage = primaryStage;
    }

    public void start() {
        VBox vbox = new VBox(15);
        TextField youtubeLinkField = new TextField();
        youtubeLinkField.setPromptText("Enter YouTube Link");

        TextField songTitleField = new TextField();
        songTitleField.setPromptText("Enter Song Title");

        Button downloadButton = new Button("Download & Save Song");
        Label feedbackLabel = new Label();

        downloadButton.setOnAction(e -> {
            String youtubeLink = youtubeLinkField.getText();
            String songTitle = songTitleField.getText();
            if (!youtubeLink.isEmpty() && !songTitle.isEmpty()) {
                // Call the downloader and save in the DB
                String filePath = MusicDownloader.downloadSong(youtubeLink, songTitle);
                if (filePath != null) {
                    addSongToDatabase(songTitle, filePath);
                    feedbackLabel.setText("Song added successfully!");
                } else {
                    feedbackLabel.setText("Failed to download the song.");
                }
            } else {
                feedbackLabel.setText("Please fill in all fields.");
            }
        });

        vbox.getChildren().addAll(youtubeLinkField, songTitleField, downloadButton, feedbackLabel);
        stage.setScene(new Scene(vbox, 400, 300));
        stage.show();
    }

    private void addSongToDatabase(String songTitle, String filePath) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/music_app", "root", "password")) {
            String sql = "INSERT INTO songs (title, file_path) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, songTitle);
            stmt.setString(2, filePath);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
