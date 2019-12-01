package com.example.batterylifepredection;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.android.hardware.SemBatteryUtils;
import com.samsung.android.util.SemLog;

import java.util.Observable;
import java.util.Observer;

import static com.example.batterylifepredection.Constants.HIGH_PERFORMANCE_MODE;
import static com.example.batterylifepredection.Constants.HIGH_PERFORMANCE_MODE_FLOATING_FEATURE;
import static com.example.batterylifepredection.Constants.HIGH_PERFORMANCE_MODE_FOR_RUT;
import static com.example.batterylifepredection.Constants.MULTI_RESOLUTION_FLOATING_FEATURE;
import static com.example.batterylifepredection.Constants.SEM_MODE_PERFORMANCE;
import static com.example.batterylifepredection.Constants.SYSTEM_SPEED_FEATURE;


public class BatteryInfoObservable extends Observable {
    private Context mContext;
    private int mBatteryLevel = 0;
    private int mBatteryPluggedState;
    private int mBatteryPhase;
    String timeLiveData;
    String myPredictTime;
    private static final long MINS_IN_HOUR = 60;
    private static final long HOURS_IN_DAY = 24;
    private static String TAG = BatteryInfoObservable.class.getSimpleName( );
    @SuppressLint("StaticFieldLeak")
    private static BatteryInfoObservable sInstance;
    private static boolean mIsInitialized = false;
    private BroadcastReceiver mBatteryLevelReceiver;
    private static Intent batteryIntent;

    private static void initialize(Context context) {
        sInstance = LazyHolder.INSTANCE;
        mIsInitialized = true;
    }

    public static BatteryInfoObservable getInstance(Context context, Intent intent) {
        if (!mIsInitialized) {
            initialize(context);
        }
        batteryIntent = intent;
        sInstance.init(context);
        return sInstance;
    }

    private static class LazyHolder {
        @SuppressLint("StaticFieldLeak")
        private static final BatteryInfoObservable INSTANCE = new BatteryInfoObservable( );
    }

    public void init(Context context) {
        mContext = context;
/*        new Thread(new Runnable( ) {
            @Override
            public void run() {
                Intent batteryIntent = mContext.getApplicationContext( ).registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                if (batteryIntent != null) {
                    getInfoFromIntent(batteryIntent);
                }
            }
        }).start( );*/
        getInfoFromIntent(batteryIntent);
    }

    private void getInfoFromIntent(Intent intent) {
        int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int online = intent.getIntExtra("online", 0);

        // update battery level
        if (currentLevel >= 0 && scale > 0) {
            mBatteryLevel = (currentLevel * 100) / scale;
        }
        mBatteryPluggedState = updatePluggedStateOfDevice(intent, online);
        if(mBatteryPluggedState == BatteryConstants.BatteryPluggedState.BATTERY_FAST_CHARGER_CONNECTED ||
                mBatteryPluggedState == BatteryConstants.BatteryPluggedState.BATTERY_FAST_WIRELESS_CONNECTED ||
                mBatteryPluggedState == BatteryConstants.BatteryPluggedState.BATTERY_USB_CONNECTED){
            Log.d("BatteryInfoObservable", "delete all from temp since charging");
            AppDatabase.getAppDatabase(mContext).userDao( ).deteleAll();
            return;
        }
        int mode = getCurrentModeForRut(mContext);
        final long time = SemBatteryUtils.getBatteryRemainingUsageTime(mContext, mode);
        timeLiveData = getTtsTimeString(mContext, time, false);
        //timeLiveData.postValue(timeString);
/*        new Handler(Looper.getMainLooper( )).post(new Runnable( ) {
            @Override
            public void run() {
                Toast.makeText(mContext, "remaining time is " + timeLiveData, Toast.LENGTH_LONG).show( );
            }
            // execute code that must be run on UI thread
        });*/
        notifyBatteryInfo( );
        Log.d("BatteryInfoObservable", "remaining time is " + time);
    }

    public static int getCurrentModeForRut(Context context) {
        int mode = 0;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context
                .POWER_SERVICE);
        if (powerManager != null) {
            mode = powerManager.isPowerSaveMode( ) ? 1 : 0;
            Log.d("BatteryInfoObservable", "power save enabled ? : " + mode);
            if (mode == 0 && isHighPerformanceModeEnabled(context)) {
                SemLog.i(TAG, "High Performance mode ");
                return HIGH_PERFORMANCE_MODE_FOR_RUT;
            } else {
                return mode;
            }
        }
        SemLog.e(TAG, "powerManager is null !! ");
        return mode;
    }

    private static boolean isHighPerformanceModeEnabled(Context context) {
        boolean isHighPerformanceEnabled = Settings.Secure.getInt(context.getContentResolver( ), SEM_MODE_PERFORMANCE, 0) == HIGH_PERFORMANCE_MODE;

        SemLog.i(TAG, "isHighPerformanceEnabled :  " + isHighPerformanceEnabled);

        return isSupportHighPerformance( ) && isHighPerformanceEnabled;
    }

    public static boolean isSupportHighPerformance() {

        boolean supportHighPerformanceFeature = SemFloatingFeature.getInstance( ).getBoolean(HIGH_PERFORMANCE_MODE_FLOATING_FEATURE, false);
        boolean supportMultiResolution = !SemFloatingFeature.getInstance( ).getString(MULTI_RESOLUTION_FLOATING_FEATURE).isEmpty( );
        boolean supportSystemSpeedFeature = SemFloatingFeature.getInstance( ).getBoolean(SYSTEM_SPEED_FEATURE, false);

        SemLog.i(TAG, "supportHighPerformanceFeature : " + supportHighPerformanceFeature
                + ", supportMultiResolution : " + supportMultiResolution
                + ", supportSystemSpeedFeature : " + supportSystemSpeedFeature);

        return supportHighPerformanceFeature && (supportMultiResolution || supportSystemSpeedFeature);
    }

    public int updatePluggedStateOfDevice(Intent intent, int online) {
        int pluggedState = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        if (intent.getBooleanExtra("hv_charger", false)) {
            pluggedState = BatteryConstants.BatteryPluggedState.BATTERY_FAST_CHARGER_CONNECTED;
        }

        if (BatteryConstants.BatteryPluggedState.BATTERY_FAST_WIRELESS_CONNECTED == online) {
            pluggedState = BatteryConstants.BatteryPluggedState.BATTERY_FAST_WIRELESS_CONNECTED;
        }

        return pluggedState;
    }

    public static String getTtsTimeString(Context context, long time, boolean speakZero) {
        String timeString;
        if (time > 0) {
            int day = getDay(time);
            int hour = getHour(time);
            int minutes = getMinutes(time);
            timeString = getTimeStringContentDescription(context, day, hour, minutes);
        } else if (speakZero) {
            timeString = context.getString(R.string.no_hour_more_minutes, 0);
        } else {
            timeString = context.getString(R.string.unavailable_text);
        }
        SemLog.v(TAG, "getTtsTimeString. time = " + time + " timeString " + timeString);
        return timeString;
    }

    private static String getTimeStringContentDescription(Context context, int day, int hour, int
            minute) {

        StringBuilder contentDesc = new StringBuilder("");
        String space = " ";

        if (day >= 1) {
            if (day == 1) {
                contentDesc.append(context.getString(R.string.one_day)).append(space);
            } else {
                contentDesc.append(context.getString(R.string.more_days, day)).append(space);
            }
        }
        if (hour >= 1) {
            if (hour == 1) {
                contentDesc.append(context.getString(R.string.one_hour_no_minute)).append(space);
            } else {
                contentDesc.append(context.getString(R.string.more_hours_no_minute, hour)).append(space);
            }
        }
        if (day < 1 && minute >= 1) { // show maximum 2 unit(d + h or h + m)
            if (minute == 1) {
                contentDesc.append(context.getString(R.string.no_hour_one_minute));
            } else {
                contentDesc.append(context.getString(R.string.no_hour_more_minutes, minute));
            }
        }
        return contentDesc.toString( );
    }

    public static int getDay(long time) {
        int day = 0;
        int hour = (int) (time / MINS_IN_HOUR);
        if (hour >= HOURS_IN_DAY) {
            day = (int) (hour / HOURS_IN_DAY);
        }
        return day;
    }

    public static int getHour(long time) {
        int hour = (int) (time / MINS_IN_HOUR);
        if (hour >= HOURS_IN_DAY) {
            hour = (int) (hour % HOURS_IN_DAY);
        }
        return hour;
    }

    public static int getMinutes(long time) {
        return (int) (time % MINS_IN_HOUR);
    }

    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver(o);
        registerReceiver( );
    }

    @Override
    public synchronized void deleteObserver(Observer o) {
        super.deleteObserver(o);
        unRegisterReceiver( );
    }

    public void registerReceiver() {
        if (countObservers( ) == 1) {
/*            mBatteryLevelReceiver = new BroadcastReceiver( ) {
                @Override
                public void onReceive(Context context, Intent intent) {
                    android.util.Log.d(TAG, "in receiver");
                    getInfoFromIntent(intent);
                }
            };

            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            mContext.registerReceiver(mBatteryLevelReceiver, batteryLevelFilter);*/
            android.util.Log.d(TAG, "registerReceiver");
        } else {
            //notifyBatteryInfo( );
            android.util.Log.d(TAG, "already registered. notify exist battery info");
        }
    }

    private void notifyBatteryInfo() {
        if (timeLiveData != null) {
            setChanged( );
            notifyObservers(timeLiveData);
        }
        android.util.Log.d(TAG, "notifyBatteryInfo");
    }

    @Override
    public synchronized int countObservers() {
        int count = super.countObservers( );
        android.util.Log.e(TAG, "countObservers : " + count);
        return count;
    }

    private void unRegisterReceiver() {
        try {
            if (mBatteryLevelReceiver != null && countObservers( ) == 0) {
                mContext.unregisterReceiver(mBatteryLevelReceiver);
                android.util.Log.d(TAG, "unRegisterReceiver");
            }
        } catch (Exception e) {
            android.util.Log.d(TAG, "Battery Receiver not registered", e);
        }
    }

}
