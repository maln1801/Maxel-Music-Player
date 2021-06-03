package se.miun.dt002g.maxel;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

public class Maxel extends Application {

    private static WeakReference<Context> sContext;
    private static Music music;

    @Override
    public void onCreate() {
        sContext = new WeakReference<Context>(getApplicationContext());
        music = new Music();
        super.onCreate();
    }

    public static void refreshMusic(){
        music = new Music();
    }

    public static Music getMusic(){
        return music;
    }

    public static Context getContext() {
        return sContext.get();
    }
}
