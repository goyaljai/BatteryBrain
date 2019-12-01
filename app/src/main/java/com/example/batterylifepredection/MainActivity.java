package com.example.batterylifepredection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import com.example.batterylifepredection.entities.TemporaryDatabase;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "blp/ " + MainActivity.class.getSimpleName();
    private final int STORAGE_REQUEST_CODE = 1;
    private BatteryInfoObservable observer;
    private Context mContext;
    private TextView mMyPredictValueTv;
    private TextView mgooglePredictValueTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        mContext = this;
        mMyPredictValueTv = findViewById(R.id.my_predict_tv);
        mgooglePredictValueTv = findViewById(R.id.google_predict_tv);
        AppDatabase.getAppDatabase(mContext).userDao().getAll().observe(this, new Observer<List<TemporaryDatabase>>() {
            @Override
            public void onChanged(List<TemporaryDatabase> temporaryDatabases) {
                if (temporaryDatabases.size() > 0) {
                    TemporaryDatabase datavalue = temporaryDatabases.get(temporaryDatabases.size() - 1);
                    mMyPredictValueTv.setText("AppPredicted : " + datavalue.getMyPredicted());
                    mgooglePredictValueTv.setText("googlePredicted : " + datavalue.getgooglePredicted());
                }
            }
        });
/*        observer = new BatteryInfoObservable(this);
        observer.getTimeLiveData( ).observe(this, new Observer <String>( ) {
            @Override
            public void onChanged(String s) {
                TemporaryDatabase tempData = new TemporaryDatabase();
                //TODO: set my predicted value
                tempData.setCurrentTime(s);
                AppDatabase.getAppDatabase(mContext).userDao().insert(tempData);
            }
        });*/
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.predict_button) {
            //predict();
            //observer.init();
        }
    }


    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == STORAGE_REQUEST_CODE) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            Utils.startService(mContext);
            Utils.predict(mContext);
        }
    }


}
