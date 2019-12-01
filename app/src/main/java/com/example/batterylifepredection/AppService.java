package com.example.batterylifepredection;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class AppService extends Service {
    private Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("AppService", "Data compare Service", NotificationManager.IMPORTANCE_MIN);
        notificationManager.createNotificationChannel(notificationChannel);

        Notification notification = new NotificationCompat.Builder(this, "AppService").setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setWhen(0)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(100, 0, true)
                .setCategory(NotificationCompat.CATEGORY_SYSTEM)
                .setTicker("CMCUIService").setDefaults(0)
                .setContentTitle("Collecting data")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();

        startForeground(88901049, notification);
        return super.onStartCommand(intent, flags, startId);
    }
}
