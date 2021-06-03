package se.miun.dt002g.maxel;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;


public class MainActivity extends AppCompatActivity {

    private static WeakReference<TextView> playing;
    private static WeakReference<MaterialButton> pause;
    private SeekBar seekBar;
    private static WeakReference<TextView> duration;
    private static WeakReference<TextView> length;
    private static WeakReference<ShapeableImageView> albumArt;
    private static WeakReference<FragmentManager> fragManager;
    private static RemoteViews contentView;
    private NotificationManager notificationManager;
    MediaService playing_service;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("BINDERSERVICE", "Creating Main");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Sends every meny-ID as a set of ids because every
        // meny is a top-level destination.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_songs, R.id.navigation_albums, R.id.navigation_artists,
                R.id.navigation_playlists, R.id.navigation_genres)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


        // Asking for permission to write in storage if not permitted
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        handler = new Handler();

        Log.d("BINDERSERVICE", "Creating connection");
        ServiceConnection serviceCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaService.LocalBinder binder = (MediaService.LocalBinder)service;
                playing_service = binder.getService();
                if(playing_service != null){
                    Log.d("BINDERSERVICE", "Service is bonded successfully!");
                    playing_service.setActivity(MainActivity.this);

                    // Successfully binded
                    // TODO Replace WeakReferences
                    playing     = new WeakReference<>(findViewById(R.id.info_title));
                    pause       = new WeakReference<>(findViewById(R.id.nav_pause));
                    seekBar     = findViewById(R.id.info_seekbar);
                    duration    = new WeakReference<>(findViewById(R.id.info_time));
                    length      = new WeakReference<>(findViewById(R.id.info_length));
                    albumArt    = new WeakReference<>(findViewById(R.id.album_art));
                    fragManager = new WeakReference<>(getSupportFragmentManager());

                    MaterialButton prev    = findViewById(R.id.nav_previous);
                    MaterialButton next    = findViewById(R.id.nav_next);
                    MaterialButton repeat  = findViewById(R.id.nav_repeat);
                    MaterialButton shuffle = findViewById(R.id.nav_shuffle);

                    prev.setOnClickListener(click ->  {
                        playing_service.prevSong();
                    });
                    next.setOnClickListener(click -> {
                        playing_service.nextSong();
                    });
                    pause.get().setOnClickListener(click -> {
                        playing_service.playPause();
                    });
                    repeat.setOnClickListener(click -> {
                        if(playing_service.getRepeat()) {
                            repeat.setIconTintResource(R.color.button_shuffle_repeat);
                            playing_service.setRepeat(false);
                        }
                        else {
                            repeat.setIconTintResource(R.color.button_shuffle_repeat_pressed);
                            playing_service.setRepeat(true);
                        }
                    });
                    shuffle.setOnClickListener(click-> {
                        if(playing_service.getRandom()) {
                            shuffle.setIconTintResource(R.color.button_shuffle_repeat);
                            playing_service.setRandom(false);
                        }
                        else {
                            shuffle.setIconTintResource(R.color.button_shuffle_repeat_pressed);
                            playing_service.setRandom(true);
                        }

                    });

                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(playing_service != null && fromUser){
                                playing_service.setProgress(progress);
                            }
                        }
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });


                    if(playing_service.isPlaying() || playing_service.isPaused()) {
                        setAlbumArt(playing_service.getCurrentlyPlaying().getArt());
                        if(!playing_service.isPaused())
                            setButtonPlaying(true);
                        else
                            setButtonPlaying(false);
                        length.get().setText(playing_service.getCurrentlyPlaying().getDurationString());
                        playing.get().setText(playing_service.getCurrentlyPlaying().getTitle());
                        seekBar.setMax(playing_service.getSongDuration());
                        startProgressbar();

                    }
                }
                else {
                    Log.d("BINDERSERVICE", "Service is not bonded!");
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                playing_service = null;
                Log.d("BINDERSERVICE", "DIsc binder");
            }
        };

        Log.d("BINDERSERVICE", "Loaded connection");

        Log.d("BINDERSERVICE", "Loading binder");

        Intent intent = new Intent(new Intent(this, MediaService.class));

        boolean bound = getApplicationContext().bindService(new Intent(intent),
                serviceCon, Context.BIND_AUTO_CREATE);

        Log.d("BINDERSERVICE", "Bound: " + bound);


    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage(R.string.instructions)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Maxel.refreshMusic();
    }

    public void updateNotification(Song song, boolean playing){
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "Maxel";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Now Playing", NotificationManager.IMPORTANCE_LOW);

        // Configuring notificationchannel
        notificationChannel.setDescription("Channel description");
        notificationChannel.enableLights(false);
        notificationChannel.enableVibration(false);
        notificationManager.createNotificationChannel(notificationChannel);

        contentView = new RemoteViews(getPackageName(), R.layout.notification);

        // If a song is playing the notification should be updated
        if(song != null) {
            String songInfo = song.getTitle() + "\n" + song.getArtist() + "\n" + song.getAlbum();
            contentView.setTextViewText(R.id.notification_song_info, songInfo);

            if(song.getArt() != null){
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {    // For older versions
                    contentView.setImageViewBitmap(R.id.notification_album_art, BitmapFactory.decodeFile(song.getArt()));
                }
                else{                                                   // For newer versions
                    try{
                        Bitmap bitmap = Maxel.getContext().getContentResolver()
                                .loadThumbnail(Uri.parse(song.getArt()), new Size(1024, 1024), null);
                        contentView.setImageViewBitmap(R.id.notification_album_art, bitmap);
                    }
                    catch(IOException e){   // Resets the "Image missing"-image.
                        contentView.setImageViewResource(R.id.notification_album_art, R.drawable.img_empty_album);
                    }

                }
            }
            else{                           // Resets the "Image missing"-image.
                contentView.setImageViewResource(R.id.notification_album_art, R.drawable.img_empty_album);
            }

            if(playing) {
                contentView.setImageViewResource(R.id.notification_playPause, R.drawable.ic_pause_24dp);
            }
            else {
                contentView.setImageViewResource(R.id.notification_playPause, R.drawable.ic_play_arrow_black_24dp);
            }
        }

        Intent iPrev = new Intent(this, ControlsService.class);
        iPrev.putExtra("Action", "Prev");
        PendingIntent prevPendingIntent = PendingIntent.getService(
                this, 1137, iPrev, PendingIntent.FLAG_UPDATE_CURRENT
        );
        contentView.setOnClickPendingIntent(R.id.notification_prev, prevPendingIntent);

        Intent iPlay = new Intent(this, ControlsService.class);
        iPlay.putExtra("Action", "PlayPause");
        PendingIntent playPendingIntent = PendingIntent.getService(
                this, 1237, iPlay, PendingIntent.FLAG_UPDATE_CURRENT
        );
        contentView.setOnClickPendingIntent(R.id.notification_playPause, playPendingIntent);

        Intent iNext = new Intent(this, ControlsService.class);
        iNext.putExtra("Action", "Next");
        PendingIntent nextPendingIntent = PendingIntent.getService(
                this, 1337, iNext, PendingIntent.FLAG_UPDATE_CURRENT
        );
        contentView.setOnClickPendingIntent(R.id.notification_next, nextPendingIntent);

        Intent iOpen = new Intent(this, ControlsService.class);
        iNext.putExtra("Action", "Open");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent appPendingIntent = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        notificationBuilder.setAutoCancel(false)
                .setVibrate(null)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setContent(contentView)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(appPendingIntent);


        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

    // Reacts to users response to permission request
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // If permission was not granted, the user is being sent back då previous activity
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            System.exit(0);
        }
    }

    public static void setPlaying(String currentSong) {
        playing.get().setText(currentSong);
    }

    public static void setAlbumArt(String art) {
        if(art != null){
            if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {    // For older versions
                albumArt.get().setImageBitmap(BitmapFactory.decodeFile(art));
            }
            else{                                                   // For newer versions
                try{
                    Bitmap bitmap = Maxel.getContext().getContentResolver()
                            .loadThumbnail(Uri.parse(art), new Size(1024, 1024), null);
                    albumArt.get().setImageBitmap(bitmap);
                }
                catch(IOException e){   // Resets "image missing"-image
                    albumArt.get().setImageDrawable(
                            ContextCompat.getDrawable(Maxel.getContext(), R.drawable.img_empty_album));
                }
            }
        }
        else{                           // Resets "image missing"-image
            albumArt.get().setImageDrawable(
                    ContextCompat.getDrawable(Maxel.getContext(), R.drawable.img_empty_album));
        }
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }
    public static TextView getDuration() {return duration.get();}
    public static TextView getLength() {return length.get();}
    public static TextView getPlaying() {return playing.get();}
    public static void setButtonPlaying(boolean shouldPlay) {
        if(shouldPlay){
            pause.get().setIconResource(R.drawable.ic_pause_24dp);
        }
        else{
            pause.get().setIconResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    public void startProgressbar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    duration.get().setText(playing_service.getProgressString(playing_service.getProgrtessInt()));
                    seekBar.setProgress(playing_service.getProgrtessInt());
                } catch (Exception e) {
                    seekBar.setProgress(0);
                }
                handler.postDelayed(this, 10);
            }
        }, 10);
    }
}