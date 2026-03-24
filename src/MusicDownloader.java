import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MusicDownloader {
    public static String downloadSong(String youtubeUrl, String songTitle) {
        try {
            String pythonScriptPath = "D:\\download_music\\download_song.py"; // Adjust if needed
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", pythonScriptPath, youtubeUrl, songTitle
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            String filePath = "";
            while ((line = reader.readLine()) != null) {
                filePath = line; // Capture the returned file path
                System.out.println(line);
            }
            process.waitFor();
            return filePath; // Return the file path to be saved in the database
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
