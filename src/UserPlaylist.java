import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.sql.*;

public class UserPlaylist extends Application {
    private MediaPlayer mediaPlayer;

    @Override
    public void start(Stage primaryStage) {
        VBox vbox = new VBox(15);
        ListView<String> songListView = new ListView<>();
        Button playButton = new Button("Play Selected Song");

        // Fetch songs from the database
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/music_app", "root", "password")) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT title, file_path FROM songs");
            while (rs.next()) {
                songListView.getItems().add(rs.getString("title") + " | " + rs.getString("file_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        playButton.setOnAction(e -> {
            String selectedItem = songListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String[] parts = selectedItem.split("\\|");
                playSong(parts[1].trim());
            }
        });

        vbox.getChildren().addAll(songListView, playButton);
        primaryStage.setScene(new Scene(vbox, 400, 300));
        primaryStage.show();
    }

    private void playSong(String filePath) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Media media = new Media(new java.io.File(filePath).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
