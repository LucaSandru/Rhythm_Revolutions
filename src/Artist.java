import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Artist implements Playable, Shareable, Comparable<Artist> {
    private String name;
    private List<Song> songs;

    public Artist(String name, List<Song> songs) {
        this.name = name;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song newSong) {
        songs.add(newSong);
        //System.out.printf(newSong + " successfully added to " + name + "'s song list\n");
    }

    public void removeSong(Song song) {
        songs.remove(song);
        //System.out.printf(song + " successfully removed from " + name + "'s song list\n");
    }

    /*public void displayArtistInfo() {
        System.out.printf("Artist: " + name);
        if (songs.isEmpty())
            System.out.println("No songs found");
        else {
            System.out.printf(", songs: ");
            for (Song song : songs)
                System.out.printf(song.getTitle() + "|");
            System.out.println();
        }
    }*/

    public void play() {
        System.out.printf("Playing all songs by: " + name + "\n");
        for (Song song : songs)
            song.play();
    }

    public void pause() {
        System.out.printf("Pausing all songs by: " + name + "\n");
    }

    public void stop() {
        System.out.printf("Stopping all songs by: " + name + "\n");
    }

    public String getShareLink() {
        return "https://open.spotify.com/artist/" + name.replaceAll(" ", "-").toLowerCase();
    }

    public int compareTo(Artist otherArtist) {
        return this.name.compareToIgnoreCase(otherArtist.name);
    }

    // Convert Artist to file-friendly format
    public String toFileString() {
        String songTitles = songs.stream().map(Song::getTitle).collect(Collectors.joining("|"));
        return String.join(",", name, songTitles);
    }

    // Recreate Artist from file-friendly format
    public static Artist fromFileString(String fileString) {
        String[] parts = fileString.split(",", 2); // Only split into two parts: name and songs
        if (parts.length < 2) return null;

        List<Song> songs = new ArrayList<>();
        for (String title : parts[1].split("\\|")) { // Parse songs from the second part
            songs.add(new Song(title));
        }

        return new Artist(parts[0], songs); // Create an artist with name and songs
    }

    public void setName(String newName) {
        this.name = newName;
    }
}
