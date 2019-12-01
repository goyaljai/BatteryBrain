package com.example.batterylifepredection;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.batterylifepredection.entities.AllData;
import com.example.batterylifepredection.entities.TemporaryDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;

public class MyApplicationClass extends Application {
    private String LOG = MyApplicationClass.class.getSimpleName();
    private final int STORAGE_REQUEST_CODE = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG, " onCreate MyApplicationClass ");
        getApplicationContext().registerReceiver(new BatteryChangeReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    static class BatteryChangeReceiver extends BroadcastReceiver {
        private String LOG = BatteryChangeReceiver.class.getSimpleName();
        private BatteryInfoObservable observer;
        Context context;
        int mBatteryLevel;

        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d(LOG, " onReceive " + intent.getAction());
            final String str = intent.getAction();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "onReceive " + str, Toast.LENGTH_LONG).show();
                }
                // execute code that must be run on UI thread
            });
            this.context = context;
            if (!AvailableBatteryLevel(intent))
                BatteryInfoObservable.getInstance(context, intent).addObserver(mBatteryObserver);
        }

        private boolean AvailableBatteryLevel(Intent intent) {
            boolean available = false;
            mBatteryLevel = -1;
            int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            // update battery level
            if (currentLevel >= 0 && scale > 0) {
                mBatteryLevel = (currentLevel * 100) / scale;
            }
            List<TemporaryDatabase> list = AppDatabase.getAppDatabase(context).userDao().getRecord(mBatteryLevel).getValue();
            if (list != null && list.size() > 0)
                available = true;
            final String toaststr = " AvailableBatteryLevel " + available + " for battery level " + mBatteryLevel;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, toaststr, Toast.LENGTH_LONG).show();
                }
                // execute code that must be run on UI thread
            });
            Log.d(LOG, toaststr);
            return available;
        }

        private final java.util.Observer mBatteryObserver = new java.util.Observer() {
            @Override
            public void update(Observable observable, Object o) {
                Log.d(LOG, " update " + o.toString());
                final String samsungvalue = o.toString();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "remaining time is " + samsungvalue, Toast.LENGTH_LONG).show();
                    }
                    // execute code that must be run on UI thread
                });
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyyy hh:mm");
                String strDate = formatter.format(date);
                TemporaryDatabase tempData = new TemporaryDatabase();
                //TODO: set my predicted value
                //Add predict function
                String mypredicted = Utils.predict(context);
                tempData.setCurrentTime(strDate);
                tempData.setgooglePredicted(samsungvalue);
                tempData.setMyPredicted(mypredicted);
                tempData.setBatteryLevel(mBatteryLevel);
                AllData alldata = new AllData(tempData);
                AppDatabase.getAppDatabase(context).userDao().insert(tempData);
                AppDatabase.getAppDatabase(context).userDao().insert(alldata);
            }
        };
    }
}
