import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlaylistTest {

    @Test
    public void testPlaylistConstructor() {
        Playlist playlist = new Playlist("Chill Hits");
        assertEquals("Chill Hits", playlist.getName());
        assertTrue(playlist.getSongs().isEmpty());
    }

    @Test
    public void testAddSongToPlaylist() {
        Playlist playlist = new Playlist("Chill Vibes");
        Song song1 = new Song("Hallelujah");

        playlist.addSong(song1);

        assertTrue(playlist.getSongs().contains(song1));
        assertEquals(1, playlist.getSongs().size());
    }

    @Test
    public void testRemoveSongFromPlaylist() {
        Playlist playlist = new Playlist("Rock Classics");
        Song song = new Song("Bohemian Rhapsody");
        playlist.addSong(song);

        playlist.removeSong(song);  // Use the method instead of directly modifying the list

        assertFalse(playlist.getSongs().contains(song));
        assertEquals(0, playlist.getSongs().size());
    }



}
