public class Song implements Playable, Comparable<Song>{
    private String title;

    Song(String title) {
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    public void resetTitle(String new_title){
        this.title = new_title;
        //System.out.printf("Update name of the song: " + this.title + "\n");
    }

    public void displaySong(){
        System.out.printf(title + " ");
    }

    public void play() {
        System.out.println("Playing song: " + title);
    }

    public void pause() {
        System.out.println("Pausing song: " + title);
    }

    public void stop() {
        System.out.println("Stopping song: " + title);
    }

    public int compareTo(Song otherSong) {
        return this.title.compareToIgnoreCase(otherSong.getTitle());
    }
}
