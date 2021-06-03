package se.miun.dt002g.maxel;

public class Genre {
    private String name;
    private long id;

    public Genre(long id, String name){
        this.id = id;
        this.name = name;
    }

    public void setName(String name) { this.name = name; }
    public void setGenreId(long genreId) { this.id = genreId; }

    public long getId() { return id; }
    public String getName() { return name; }
}
