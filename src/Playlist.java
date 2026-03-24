import java.util.*;

public class Playlist {
    private String name;
    private List<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public void addSong(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
        }
    }


    public void removeSong(Song song) {
        songs.remove(song);
    }

}

