package com.gpstracker.anton;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.gpstracker.anton.Server.DataServer;

import java.util.HashMap;

public class OnClearFromRecentService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ClearFromRecentService", "Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ClearFromRecentService", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("ClearFromRecentService", "END");
        HashMap<String, String> prop = new MyProperties().getProp(this);

        HashMap<String, String> user = new DataServer().getUser(prop.get("email"));
        HashMap<String, String> roomusers = new DataServer().getRoom(user.get("room"));

        new DataServer().updateUserRoom(prop.get("email"), user.get("room"));
        new DataServer().updateRoom(prop.get("email"), roomusers.get("users"), user.get("room"), true);
        //Code here
        stopSelf();
    }
}