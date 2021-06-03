package se.miun.dt002g.maxel.ui.playlists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import se.miun.dt002g.maxel.Maxel;
import se.miun.dt002g.maxel.Playlist;

public class PlaylistsViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> playlists;

    public PlaylistsViewModel() {
        ArrayList<Playlist> allPlaylists = Maxel.getMusic().getPlaylists();
        ArrayList<String> names = new ArrayList<>();

        if(allPlaylists != null){
            for(Playlist playlist : allPlaylists){
                names.add(playlist.getName());
            }
        }

        playlists = new MutableLiveData<>(names);
    }

    public LiveData<ArrayList<String>> getPlaylists() {
        return playlists;
    }
}