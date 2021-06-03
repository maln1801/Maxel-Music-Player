package se.miun.dt002g.maxel;

public class Album {
    private String title, art;
    private long id;

    public Album(long id, String title){
        this.id = id;
        this.title = title;
    }

    public void setTitle(String album) { this.title = album; }
    public void setAlbumId(long albumId) { this.id = albumId; }
    public void setArt(String art) { this.art = art; }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArt() { return art; }
}
