package se.miun.dt002g.maxel;

public class Artist {
    private String name;
    private long id;

    public Artist(long id, String name){
        this.id = id;
        this.name = name;
    }

    public void setName(String name) { this.name = name; }
    public void setArtistId(long artistId) { this.id = artistId; }

    public long getId() { return id; }
    public String getName() { return name; }
}
