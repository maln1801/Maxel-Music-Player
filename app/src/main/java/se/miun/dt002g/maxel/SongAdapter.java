package se.miun.dt002g.maxel;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {
    private final List<Song> songs;
    public Context context;
    public boolean onPlaylistsPage = false;
    public ViewGroup parent;
    private MediaService.LocalBinder binder;
    private boolean bound = false;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public ImageView albumArtImageView;
        public TextView albumAndDuration;
        public MyViewHolder(View theView) {
            super(theView);
            titleTextView = theView.findViewById(R.id.title);
            albumArtImageView = theView.findViewById(R.id.album_art_row);
            albumAndDuration = theView.findViewById(R.id.album_duration);
        }
    }

    public SongAdapter(List<Song> songs) {
        this.songs = songs;
    }

    // Skapar nya views
    @Override
    public SongAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);
        this.parent = parent;
        return new MyViewHolder(v);
    }

    // Ersätter inehållet av view
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.titleTextView.setText(songs.get(position).getTitle());
        holder.albumAndDuration.setText(
                songs.get(position).getArtist() + "    •   " +
                    songs.get(position).getAlbum() + "    •   " +
                        songs.get(position).getDurationString());
        setAlbumArt(songs.get(position).getArt(), holder.albumArtImageView);


        ServiceConnection serviceCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MediaService.LocalBinder) service;
                Log.d("BINDERSERVICE", "Song adapter connected to Binder");
                bound = true;

                holder.itemView.setOnClickListener(v -> {
                    Log.d("SONGPATH", songs.get(position).getPath());
                    try {
                        if (bound)
                            binder.getService().PlaySong(songs.get(position));
                        // "Queues" following (and previous) songs for playback.
                        if (bound)
                            binder.getService().clearQueue();

                        for(Song song : songs){
                            if (bound)
                                binder.getService().addtoQueue(song);
                        }
                        if (bound)
                            binder.getService().setIndex(position);

                    } catch (Exception e) {
                        Log.d("PLAYERROR", "Could not play song: " + songs.get(position).getTitle());
                        e.printStackTrace();
                    }
                });


                holder.itemView.setOnLongClickListener(View::showContextMenu);
                holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.addSubMenu(Menu.NONE, R.id.add_to_playlist, Menu.NONE, "Add to playlist...");
                    SubMenu addMenu = menu.findItem(R.id.add_to_playlist).getSubMenu();
                    AtomicReference<String> playlistName = new AtomicReference<>();
                    playlistName.set("");
                    ArrayList<Playlist> lists = Maxel.getMusic().getPlaylists();

                    addMenu.add("Create new playlist...").setOnMenuItemClickListener(item -> {
                        EditText editText = new EditText(holder.itemView.getContext());

                        new AlertDialog.Builder(holder.itemView.getContext())
                                .setTitle("New Playlist")
                                .setMessage("Enter a Playlist name")
                                .setView(editText)
                                .setPositiveButton("That's it!", (dialog, whichButton) -> {

                                    playlistName.set(editText.getText().toString());
                                    Log.d("PLAYLIST", "User entered: " + playlistName.get());
                                    if(!playlistName.get().isEmpty()) {
                                        Playlist newPlaylist = new Playlist(playlistName.get());
                                        newPlaylist.addSong(songs.get(position));
                                        Maxel.getMusic().addPlaylist(newPlaylist);
                                        Maxel.getMusic().savePlaylists();
                                        Log.d("PLAYLIST", "Added playlist: " + newPlaylist.getName());
                                    }
                                }).setNegativeButton("Nevermind", (dialog, whichButton) -> {
                        }).show();

                        return true;
                    });


                    if(lists != null){
                        for(Playlist list : lists){
                            addMenu.add("Add to " + list.getName()).setOnMenuItemClickListener(item -> {
                                list.addSong(songs.get(position));
                                return true;
                            });
                        }


                        if(onPlaylistsPage){
                            menu.add("Remove from playlist").setOnMenuItemClickListener(item -> {
                                LinearLayout parentRec = (LinearLayout) parent.getParent();
                                TextView listNameView = parentRec.findViewById(R.id.parent_title);
                                String listName = (String) listNameView.getText();

                                for(Playlist list: lists){
                                    if(list.getName().equals(listName)){
                                        list.removeSong(position);
                                        notifyDataSetChanged();
                                    }
                                }

                                return true;
                            });
                        }
                    }
                });
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder = null;
                Log.d("BINDERSERVICE", "Song adapter disconnected from Binder");
                bound = false;
            }
        };

        Intent intent = new Intent(Maxel.getContext(), MediaService.class);
        Maxel.getContext().bindService(intent, serviceCon, Context.BIND_AUTO_CREATE);


    }

    // Returns all loaded items
    @Override
    public int getItemCount() {
        return songs.size();
    }

    public static void setAlbumArt(String art, ImageView albumArt) {
        if(art != null){
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {    // For older versions
                albumArt.setImageBitmap(BitmapFactory.decodeFile(art));
            }
            else{                                                   // For newer versions
                try{
                    Bitmap bitmap = Maxel.getContext().getContentResolver()
                            .loadThumbnail(Uri.parse(art), new Size(48, 48), null);
                    albumArt.setImageBitmap(bitmap);
                }
                catch(IOException e){   // Resets "Image missing"-image
                    albumArt.setImageDrawable(
                            Maxel.getContext().getDrawable(R.drawable.img_empty_album));
                }

            }
        }
        else{                           // Resets "Image missing"-image
            albumArt.setImageDrawable(Maxel.getContext().getDrawable(R.drawable.img_empty_album));
        }
    }

    public void setOnPlaylistsPage(boolean status) { this.onPlaylistsPage = status ;}
}