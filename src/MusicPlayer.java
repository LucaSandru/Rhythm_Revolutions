import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class MusicPlayer {
    private MediaPlayer mediaPlayer;

    public void playSong(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
            } else {
                System.out.println("File not found: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
