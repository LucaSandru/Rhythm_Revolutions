public class Genre {
    private String genreName;

    Genre(String genreName) {
        this.genreName = genreName;
    }

    public String getGenreName() {
        return genreName;
    }

    public void resetGenreName(String genreName) {
        this.genreName = genreName;
    }

    public void displayGenre(){
        System.out.printf("Genre: " + genreName + "\n");
    }
}
