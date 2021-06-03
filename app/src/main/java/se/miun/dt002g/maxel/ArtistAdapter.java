package se.miun.dt002g.maxel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.MyViewHolder> {
    private List<String> artists;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public RecyclerView songsView;
        public ImageView albumArtImageView;
        public MyViewHolder(View theView) {
            super(theView);
            titleTextView = theView.findViewById(R.id.parent_title);
            songsView = theView.findViewById(R.id.recycler_songs);
            albumArtImageView = theView.findViewById(R.id.album_art_parent);
        }
    }

    public ArtistAdapter(List<String> artists) {
        this.artists = artists;
    }

    // Skapar nya views
    @Override
    public ArtistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_parent, parent, false);
        return new MyViewHolder(v);
    }

    // Ersätter inehållet av view
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AtomicBoolean expanded = new AtomicBoolean(false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Maxel.getContext(), LinearLayoutManager.VERTICAL, false);

        List<Song> songs = Maxel.getMusic().getSongsByArtist(artists.get(position));
        SongAdapter childRecyclerViewAdapter = new SongAdapter(songs);

        holder.songsView.setLayoutManager(layoutManager);
        holder.songsView.setAdapter(childRecyclerViewAdapter);
        holder.titleTextView.setText(artists.get(position));
        holder.albumArtImageView.setVisibility(View.GONE);
        // Gömmer låtar
        holder.songsView.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            // Användaren klickar

            if (!expanded.get()) {
                // Visar låtar
                holder.songsView.setVisibility(View.VISIBLE);
                expanded.set(true);
            } else {
                // Gömmer låtar
                holder.songsView.setVisibility(View.GONE);
                expanded.set(false);
            }

        });
    }

    // Returnerar antalet ilästa föremål
    @Override
    public int getItemCount() {
        return artists.size();
    }

}