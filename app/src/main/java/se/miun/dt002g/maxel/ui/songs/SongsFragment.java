package se.miun.dt002g.maxel.ui.songs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import se.miun.dt002g.maxel.Maxel;
import se.miun.dt002g.maxel.Music;
import se.miun.dt002g.maxel.R;
import se.miun.dt002g.maxel.SongAdapter;

public class SongsFragment extends Fragment {

    private SongsViewModel songsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        songsViewModel =
                new ViewModelProvider(this).get(SongsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_songs, container, false);
        final RecyclerView recView = root.findViewById(R.id.recycler_songs);
        songsViewModel.getSongs().observe(getViewLifecycleOwner(), new Observer<Music>() {
            @Override
            public void onChanged(@Nullable Music m) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(Maxel.getContext());
                recView.setLayoutManager(layoutManager);
                SongAdapter adapter = new SongAdapter(m.getSongs());
                recView.setAdapter(adapter);
            }
        });


        return root;
    }
}