import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class ArtistTest {

    @Test
    public void testArtistConstructor() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("Song 1"));
        Artist artist = new Artist("Coldplay", songs);

        assertEquals("Coldplay", artist.getName());
        assertEquals(1, artist.getSongs().size());
        assertEquals("Song 1", artist.getSongs().get(0).getTitle());
    }

    @Test
    public void testFollowArtist() {
        User user = new User("John Doe", "password123", "john@example.com");
        user.followArtist("The Beatles");

        assertTrue(user.getFollowedArtists().contains("The Beatles"));
        assertEquals(1, user.getFollowedArtists().size());
    }

}
