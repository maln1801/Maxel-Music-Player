package se.miun.dt002g.maxel.ui.artists;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import se.miun.dt002g.maxel.Artist;
import se.miun.dt002g.maxel.Maxel;

public class ArtistsViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> artists;

    public ArtistsViewModel() {
        ArrayList<Artist> allArtists = Maxel.getMusic().getArtists();
        ArrayList<String> names = new ArrayList<>();

        for(Artist artist : allArtists){
            names.add(artist.getName());
        }

        artists = new MutableLiveData<>(names);
    }

    public LiveData<ArrayList<String>> getArtists() {
        return artists;
    }
}