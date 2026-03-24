import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailVerification {
    @SuppressWarnings("deprecation")
    public static boolean isEmailFormatValid(String email) {
        String accessKey = "200602ebb60f0a235afaea636c0b0b2c";
        String urlString = "http://apilayer.net/api/check?access_key=" + accessKey + "&email=" + email + "&smtp=1&format=0";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            String response = content.toString();
            return response.contains("\"mx_found\":true");

        } catch (Exception e) {
            System.out.println("Error: Unable to connect to the email verification service.");
            return false;
        }
    }
}
