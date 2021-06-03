package se.miun.dt002g.maxel.ui.genres;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import se.miun.dt002g.maxel.Genre;
import se.miun.dt002g.maxel.Maxel;

public class GenresViewModel extends ViewModel {

    private MutableLiveData<ArrayList<String>> genres;

    public GenresViewModel() {
        ArrayList<Genre> allGenres = Maxel.getMusic().getGenres();
        ArrayList<String> names = new ArrayList<>();

        for(Genre genre : allGenres){
            if(!names.contains(genre.getName())) {
                names.add(genre.getName());
            }
        }

        genres = new MutableLiveData<>(names);
    }

    public LiveData<ArrayList<String>> getGenres() {
        return genres;
    }
}