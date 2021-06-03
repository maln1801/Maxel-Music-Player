package se.miun.dt002g.maxel.ui.now_playing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import se.miun.dt002g.maxel.R;

public class NowPlayingFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root       = inflater.inflate(R.layout.fragment_nowplaying, container, false);
        TextView songDuration = root.findViewById(R.id.info_time);
        TextView songTitle = root.findViewById(R.id.info_title);
        TextView songLength = root.findViewById(R.id.info_length);

        songTitle.setText("Select a song to play!");
        songDuration.setText("--:--");
        songLength.setText("--:--");

        return root;
    }


}