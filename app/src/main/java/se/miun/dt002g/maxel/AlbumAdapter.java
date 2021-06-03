package se.miun.dt002g.maxel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyViewHolder> {
    private List<String> albums;

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

    public AlbumAdapter(List<String> albums) {
        this.albums = albums;
    }

    // Creates new views
    @Override
    public AlbumAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_parent, parent, false);
        return new MyViewHolder(v);
    }

    // Replaces content of view
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AtomicBoolean expanded = new AtomicBoolean(false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Maxel.getContext(), LinearLayoutManager.VERTICAL, false);

        List<Song> songs = Maxel.getMusic().getSongsByAlbum(albums.get(position));
        SongAdapter childRecyclerViewAdapter = new SongAdapter(songs);

        holder.songsView.setLayoutManager(layoutManager);
        holder.songsView.setAdapter(childRecyclerViewAdapter);
        holder.titleTextView.setText(albums.get(position));
        setAlbumArt(songs.get(0).getArt(), holder.albumArtImageView);


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
    }

    // Returns all loaded items
    @Override
    public int getItemCount() {
        return albums.size();
    }


    public static void setAlbumArt(String art, ImageView albumArt) {
        if(art != null){
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {    // For older versions
                albumArt.setImageBitmap(BitmapFactory.decodeFile(art));
            }
            else{                                                   // For newer versions
                try{
                    Bitmap bitmap = Maxel.getContext().getContentResolver()
                            .loadThumbnail(Uri.parse(art), new Size(1024, 1024), null);
                    albumArt.setImageBitmap(bitmap);
                }
                catch(IOException e){   // Resets the "Image missing"-image.
                    albumArt.setImageDrawable(
                            Maxel.getContext().getDrawable(R.drawable.img_empty_album));
                }

            }
        }
        else{                           // Resets the "Image missing"-image.
            albumArt.setImageDrawable(Maxel.getContext().getDrawable(R.drawable.img_empty_album));
        }
    }
}