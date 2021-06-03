package se.miun.dt002g.maxel;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Song {
    private String title, artist, album, genre, path, art;
    private long id, albumId, artistId, genreId, duration;

    public Song(long id, String title, String path, long duration){
        this.id = id;
        this.title = title;
        this.path = path;
        this.duration = duration;

        this.artist = "Unknown";
        this.album = "Unknown";
        this.genre = "Unknown";
    }

    public void setArtist(String artist) { this.artist = artist; }
    public void setArtistId(long artistId) { this.artistId = artistId; }
    public void setAlbum(String album) { this.album = album; }
    public void setAlbumId(long albumId) { this.albumId = albumId; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setGenreId(long genreId) { this.genreId = genreId; }
    public void setArt(String art) { this.art = art; }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public long getArtistId() { return artistId; }
    public String getAlbum() { return album; }
    public long getAlbumId() { return albumId; }
    public String getGenre() { return genre; }
    public long getGenreId() { return genreId; }
    public String getPath() { return path; }
    public String getArt() { return art; }
    public String getDurationString() {
        return String.format(Locale.getDefault(),"%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
    }
}
