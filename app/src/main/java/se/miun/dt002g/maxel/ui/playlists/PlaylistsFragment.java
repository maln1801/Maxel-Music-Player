package se.miun.dt002g.maxel.ui.playlists;

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

import java.util.ArrayList;

import se.miun.dt002g.maxel.Maxel;
import se.miun.dt002g.maxel.PlaylistAdapter;
import se.miun.dt002g.maxel.R;

public class PlaylistsFragment extends Fragment {

    private PlaylistsViewModel playlistsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        playlistsViewModel =
                new ViewModelProvider(this).get(PlaylistsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_playlists, container, false);
        final RecyclerView recView = root.findViewById(R.id.recycler_playlists);
        playlistsViewModel.getPlaylists().observe(getViewLifecycleOwner(), new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(@Nullable ArrayList<String> list) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(Maxel.getContext());
                recView.setLayoutManager(layoutManager);
                PlaylistAdapter adapter = new PlaylistAdapter(list);
                recView.setAdapter(adapter);
            }
        });
        return root;
    }
}