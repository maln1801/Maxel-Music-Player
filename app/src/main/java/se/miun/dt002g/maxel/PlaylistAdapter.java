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

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.MyViewHolder> {
    private List<String> playlists;

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

    public PlaylistAdapter(List<String> playlists) {
        this.playlists = playlists;
    }

    // Creates new views
    @Override
    public PlaylistAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_parent, parent, false);
        return new MyViewHolder(v);
    }

    // Replaces content of views
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AtomicBoolean expanded = new AtomicBoolean(false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Maxel.getContext(), LinearLayoutManager.VERTICAL, false);

        List<Song> songs = Maxel.getMusic().getPlaylists().get(position).getSongs();
        SongAdapter childRecyclerViewAdapter = new SongAdapter(songs);
        childRecyclerViewAdapter.setOnPlaylistsPage(true);

        holder.songsView.setLayoutManager(layoutManager);
        holder.songsView.setAdapter(childRecyclerViewAdapter);
        holder.titleTextView.setText(playlists.get(position));
        holder.albumArtImageView.setVisibility(View.GONE);
        // Hides songs
        holder.songsView.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            // User clicks

            if (!expanded.get()) {
                // Shows songs
                holder.songsView.setVisibility(View.VISIBLE);
                expanded.set(true);
            } else {
                // Hides songs
                holder.songsView.setVisibility(View.GONE);
                expanded.set(false);
            }

        });


        holder.itemView.setOnLongClickListener(View::showContextMenu);
        holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add("Remove playlist").setOnMenuItemClickListener(item -> {
                Maxel.getMusic().getPlaylists().remove(position);
                Maxel.getMusic().savePlaylists();
                playlists.remove(position);
                notifyDataSetChanged();
                return true;
            });
        });
    }

    // Returns all loaded items
    @Override
    public int getItemCount() {
        return playlists.size();
    }

}