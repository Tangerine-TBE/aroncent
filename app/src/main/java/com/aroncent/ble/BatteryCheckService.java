package com.aroncent.ble;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BatteryCheckService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
