package se.miun.dt002g.maxel;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ControlsService extends Service {

    private MediaService.LocalBinder binder;
    private boolean bound = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Intent", intent.getExtras().getString("Action"));

        ServiceConnection serviceCon = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MediaService.LocalBinder) service;
                Log.d("BINDERSERVICE", "ControlService connected to Binder");
                bound = true;
                if (intent.hasExtra("Action")) {
                    switch (intent.getExtras().getString("Action")) {
                        case "PlayPause":
                            if(bound)
                                binder.getService().playPause();
                            break;
                        case "Prev":
                            binder.getService().prevSong();
                            break;
                        case "Next":
                            binder.getService().nextSong();
                            break;
                    }
                }


            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                binder = null;
                Log.d("BINDERSERVICE", "ControlService disconnected from Binder");
                bound = false;
            }
        };

        Intent intentMedia = new Intent(Maxel.getContext(), MediaService.class);
        Maxel.getContext().bindService(intentMedia, serviceCon, Context.BIND_AUTO_CREATE);

        return Service.START_NOT_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }
}
