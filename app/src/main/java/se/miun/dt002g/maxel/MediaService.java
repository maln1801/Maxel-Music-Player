package se.miun.dt002g.maxel;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.service.media.MediaBrowserService;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class MediaService extends MediaBrowserService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private List<Song> queue;
    private int index = 0;
    private int resumePosition;
    private boolean paused, repeat, random;
    private Handler handler;
    private Song currentlyPlaying;
    private MainActivity mainActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BINDERSERVICE", "MediaService: Service created");
        initialize();
    }

    public void setActivity (MainActivity activity) {
        this.mainActivity = activity;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("BINDERSERVICE", "MediaService: Something bounded: " + intent.getDataString());
        return iBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BINDERSERVICE", "MediaService: Connection closed");
        if (mediaPlayer != null) mediaPlayer.release();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {
    }

    private void initialize() {
        mediaPlayer = new MediaPlayer();
        queue = new Vector<>();
        paused = false;
        repeat = false;
        random = false;
        handler = new Handler();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextSong();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        resumePosition = 0;
        mainActivity.getSeekBar().setMax(mediaPlayer.getDuration());
        MainActivity.getLength().setText(getProgressString(mediaPlayer.getDuration()));
        MainActivity.getPlaying().setSelected(true);
        mp.start();
        MainActivity.setButtonPlaying(true);
        mainActivity.startProgressbar();
    }

    public void PlaySong(Song song) {
        if(song != null) {
            try {

                mediaPlayer.reset();
                mediaPlayer.setDataSource(song.getPath());
                MainActivity.setPlaying(song.getTitle());
                MainActivity.setAlbumArt(song.getArt());
                currentlyPlaying = song;
                // Preparing the player on a seperate thread
                mediaPlayer.prepareAsync();
                if(mainActivity != null) {
                    mainActivity.updateNotification(song, true);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void nextSong() {
        if(queue.isEmpty())
            return;
        // If there is one song in the list
        // Reset the player
        if(queue.size()-1 == index) {
            setProgress(0);
            stop();
            MainActivity.setButtonPlaying(false);
        }
        // If a song is not alone in the list
        if(random && queue.size() > 1){
            // If it is okay that the same song can be randomly played
            // Several times in a row
            //PlaySong(queue.get(new Random().nextInt(queue.size())));

            // If random does not play same song twice
            int newIndex = index;
            while(newIndex == index){
                newIndex = new Random().nextInt(queue.size());
            }

            PlaySong(queue.get(index = newIndex));
        }
        else if(queue.size() > index + 1) {
            PlaySong(queue.get(++index));
        }
        else if(repeat){
            PlaySong(queue.get(index = 0));
        }
    }

    public void prevSong() {
        if(!queue.isEmpty() && index > 0) {
            Song temp = queue.get(--index);
            PlaySong(temp);
        }
    }

    public void pause() {
        mediaPlayer.pause();
        paused = true;
        resumePosition = mediaPlayer.getCurrentPosition();
        MainActivity.setButtonPlaying(false);
        mainActivity.updateNotification(currentlyPlaying, false);
    }

    public void play() {
        if(currentlyPlaying != null) {
            if(paused) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(resumePosition);
                    mediaPlayer.start();
                    mainActivity.updateNotification(currentlyPlaying, true);
                }

            } else {
                PlaySong(currentlyPlaying);
                mainActivity.updateNotification(currentlyPlaying, true);
            }
            MainActivity.setButtonPlaying(true);
        }

    }

    public void stop() {
        if(mediaPlayer == null)
            return;
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public Song getCurrentlyPlaying() {
        return currentlyPlaying;
    }

    public void playPause() {
        if(mediaPlayer.isPlaying())
            pause();
        else
            play();
    }

    public void addtoQueue(Song song) { queue.add(song); }

    public void clearQueue() { queue.clear(); }

    public void setIndex(int i) { index = i; }

    public boolean isPlaying() {return mediaPlayer.isPlaying();}
    public boolean isPaused() {return !mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() > 1;}

    public void setRandom(boolean random) { this.random = random; }
    public boolean getRandom() { return random; }

    public void setRepeat(boolean repeat) { this.random = repeat; }
    public boolean getRepeat() { return repeat; }

    public void setProgress(int progress) {
        mediaPlayer.seekTo(progress);
        resumePosition = mediaPlayer.getCurrentPosition();
        Log.d("SEEK", "Seeking to: " + progress);
    }

    public int getProgrtessInt() {
        return mediaPlayer.getCurrentPosition();
    }
    public String getProgressString(int millis) {
        return String.format(Locale.getDefault(),"%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public class LocalBinder extends Binder {
        public MediaService getService() {
            return MediaService.this;
        }
    }

    public void resetProgressbar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    MainActivity.getDuration().setText(getProgressString(mediaPlayer.getCurrentPosition()));
                    mainActivity.getSeekBar().setProgress(mediaPlayer.getCurrentPosition());
                    resumePosition = mediaPlayer.getCurrentPosition();
                } catch (Exception e) {
                    mainActivity.getSeekBar().setProgress(0);
                }
                handler.postDelayed(this, 10);
            }
        }, 10);
    }

    public int getSongDuration() {
        return mediaPlayer.getDuration();
    }

}


