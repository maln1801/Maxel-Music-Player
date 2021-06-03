package se.miun.dt002g.maxel;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Music {
    ArrayList<Song> songs = new ArrayList<>();
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Artist> artists = new ArrayList<>();
    ArrayList<Genre> genres = new ArrayList<>();
    ArrayList<Playlist> playlists = new ArrayList<>();

    public Music(){
        // Songs
        Cursor songCursor = Maxel.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST},
                null, null, null);
        while(songCursor.moveToNext()) {
            Log.d("ADDINGMUSIC", "Queried song: " + songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            // Skips the response if it is null
            if(!songCursor.isNull(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE))){
                long id = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                long albumId = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long artistId = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                long duration = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                String name = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String path = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String artist = songCursor.getString(songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                Log.d("SONGPATH", path);
                Song newSong = new Song(id, name, path, duration);
                newSong.setAlbum(album);
                newSong.setAlbumId(albumId);
                newSong.setArtistId(artistId);
                newSong.setArtist(artist);
                songs.add(newSong);
            }

        }

        // Artists
        Cursor artistCursor = Maxel.getContext().getContentResolver().query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Artists.ARTIST, MediaStore.Audio.Artists._ID},
                null,
                null,
                MediaStore.Audio.Artists.ARTIST + " ASC");
        while(artistCursor.moveToNext()){
            // Skips if response is null
            if(!artistCursor.isNull(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)))
                artists.add(new Artist(
                        artistCursor.getLong(artistCursor.getColumnIndex(MediaStore.Audio.Artists._ID)),
                        artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST))
                ));
        }

        // Album
        try{
            Cursor albumCursor = Maxel.getContext().getContentResolver().query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.Albums.ALBUM_ART},
                    null,
                    null,
                    MediaStore.Audio.Albums.ALBUM + " ASC");
            while(albumCursor.moveToNext()){
                // Skips if response is null
                if(!albumCursor.isNull(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))){
                    Album newAlbum = new Album(
                            albumCursor.getLong(albumCursor.getColumnIndex(MediaStore.Audio.Albums._ID)),
                            albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
                    );

                    String art = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                    if(art != null){
                        newAlbum.setArt(art);
                    }
                    else{
                        art = ContentUris.withAppendedId(
                                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, newAlbum.getId())
                                .toString();

                        newAlbum.setArt(art);
                    }

                    albums.add(newAlbum);
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        // Genres
        for(Song song : songs) {
            Cursor genreCursor = Maxel.getContext().getContentResolver().query(
                    MediaStore.Audio.Genres.getContentUriForAudioId("external",
                            (int)song.getId()),
                    new String[]{MediaStore.Audio.Genres.NAME, MediaStore.Audio.Genres._ID},
                    null,
                    null,
                    MediaStore.Audio.Genres.NAME + " ASC");
            while(genreCursor.moveToNext()){
                // Skips if response is null
                if(!genreCursor.isNull(genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)))
                    genres.add(new Genre(
                            genreCursor.getLong(genreCursor.getColumnIndex(MediaStore.Audio.Genres._ID)),
                            genreCursor.getString(genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME))
                    ));

                    song.setGenre(genreCursor.getString(genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)));
                    song.setGenreId(genreCursor.getLong(genreCursor.getColumnIndex(MediaStore.Audio.Genres._ID)));
            }

            // Album art
            for(Album album : albums){
                if(album.getId() == song.getAlbumId()){
                    song.setArt(album.getArt());
                    break;
                }
            }
        }

        // Playlists
        loadPlaylists();
    }

    public ArrayList<String> getTitles(){
        ArrayList<String> titles = new ArrayList<String>();
        for(Song song : songs){
            titles.add(song.getTitle());
        }
        return titles;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public ArrayList<Album> getAlbums() { return albums; }

    public ArrayList<Artist> getArtists() { return artists; }

    public ArrayList<Genre> getGenres() { return genres; }

    public ArrayList<Playlist> getPlaylists() { return playlists; }

    public void addPlaylist(Playlist list) { playlists.add(list); }

    // TODO: Optimera/snygga till, mycket utrymme för förbättring i sökandet!
    public ArrayList<Song> getSongsByAlbum(String albumTitle) {
        ArrayList<Song> albumSongs = new ArrayList<>();
        long albumId = -1;

        for(Album album : albums){
            try{
                if(album.getTitle().equals(albumTitle)) {
                    albumId = album.getId();
                    break;
                }
            }catch (Exception e) {
                CharSequence text = "Error loading song: " + e;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(Maxel.getContext(), text, duration);
                toast.show();
            }
        }

        for(Song song : songs){
            if(song.getAlbumId() == albumId)
                albumSongs.add(song);
        }

        return albumSongs;
    }


    // TODO Optimize, much room for improvement in searching
    public ArrayList<Song> getSongsByArtist(String artistName) {
        Log.d("ARTISTS", "Searching: " + artistName);

        ArrayList<Song> artistSongs = new ArrayList<>();
        long artistId = -1;

        for(Artist artist : artists){
            try{
                if(artist.getName().equals(artistName)) {
                    artistId = artist.getId();
                    break;
                }
            } catch (Exception e) {
                CharSequence text = "Error loading artistname: " + e;
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(Maxel.getContext(), text, duration);
                toast.show();
            }

        }

        for(Song song : songs){
            if(song.getArtistId() == artistId)
                artistSongs.add(song);
        }

        return artistSongs;

    }

    // TODO Optimize, much room for improvement in searching
    public ArrayList<Song> getSongsByGenre(String genreName) {
        ArrayList<Song> genreSongs = new ArrayList<>();
        long genreId = -1;

        for(Genre genre : genres){
            if(genre.getName().equals(genreName)) {
                genreId = genre.getId();
                break;
            }
        }

        for(Song song : songs){
            if(song.getGenreId() == genreId)
                genreSongs.add(song);
        }

        return genreSongs;
    }

    public void loadPlaylists () {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Maxel.getContext());
        Gson gson = new Gson();
        String json = prefs.getString("playlists", null);
        Type type = new TypeToken<ArrayList<Playlist>>() {}.getType();
        playlists = gson.fromJson(json, type);


        // If no playlists were found
        if(playlists == null){
            playlists = new ArrayList<>();
        }
        else {
            for(Playlist p: playlists) {
                Log.d("PLAYLIST", "Loaded playlist: " + p.getName());
            }
        }

    }

    public void savePlaylists() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Maxel.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(playlists);
        editor.putString("playlists", json);
        editor.apply();
    }
}
