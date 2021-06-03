package se.miun.dt002g.maxel.ui.songs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import se.miun.dt002g.maxel.Maxel;
import se.miun.dt002g.maxel.Music;

public class SongsViewModel extends ViewModel {

    private MutableLiveData<Music> songs;

    public SongsViewModel() {
        songs = new MutableLiveData<Music>(Maxel.getMusic());
    }

    public LiveData<Music> getSongs() {
        return songs;
    }

}