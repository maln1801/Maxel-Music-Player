package se.miun.dt002g.maxel.ui.albums;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import se.miun.dt002g.maxel.Album;
import se.miun.dt002g.maxel.Maxel;

public class AlbumsViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> albums;

    public AlbumsViewModel() {
        ArrayList<Album> allAlbums = Maxel.getMusic().getAlbums();
        ArrayList<String> titles = new ArrayList<>();

        for(Album album : allAlbums){
            titles.add(album.getTitle());
        }

        albums = new MutableLiveData<>(titles);
    }

    public LiveData<ArrayList<String>> getAlbums() {
        return albums;
    }
}