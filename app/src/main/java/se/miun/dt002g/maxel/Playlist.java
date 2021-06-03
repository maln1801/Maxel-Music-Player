package se.miun.dt002g.maxel;

import java.util.ArrayList;

public class Playlist {
    private String name;
    ArrayList<Song> songs = new ArrayList<>();

    public Playlist(String name){
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ArrayList<Song> getSongs() { return songs; }
    public void setSongs(ArrayList<Song> songs) { this.songs = songs; }

    public void addSong(Song song) {
        songs.add(song);
        Maxel.getMusic().savePlaylists();
    }

    public void removeSong(int index) {
        songs.remove(index);
        Maxel.getMusic().savePlaylists();
    }

    public void removeSong(Song song) {
        songs.remove(song);
        Maxel.getMusic().savePlaylists();
    }
}
