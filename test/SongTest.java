import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SongTest {

    @Test
    public void testSongConstructor() {
        Song song = new Song("Despacito");
        assertEquals("Despacito", song.getTitle());
    }

    @Test
    public void testSongTitleUpdate() {
        Song song = new Song("Old Title");
        song.resetTitle("New Title");
        assertEquals("New Title", song.getTitle());
    }

    @Test
    public void testPreventDuplicateSongs() {
        Playlist playlist = new Playlist("Pop Hits");
        Song song = new Song("Shape of You");
        playlist.addSong(song);
        playlist.addSong(song);  // Should not be added twice

        assertEquals(1, playlist.getSongs().size());  // Should remain 1 if no duplicates allowed
    }



}
