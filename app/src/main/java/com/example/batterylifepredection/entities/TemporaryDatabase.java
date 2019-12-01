package com.example.batterylifepredection.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tempdata")
public class TemporaryDatabase {

    private int uid;
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "current_time")
    public String currentTime;

    @ColumnInfo(name = "my_predicted")
    public String myPredicted;

    @ColumnInfo(name = "google_predicted")
    public String googlePredicted;

    @ColumnInfo(name = "battery_level")
    public int batteryLevel;

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }


    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getMyPredicted() {
        return myPredicted;
    }

    public void setMyPredicted(String myPredicted) {
        this.myPredicted = myPredicted;
    }

    public String getgooglePredicted() {
        return googlePredicted;
    }

    public void setgooglePredicted(String googlePredicted) {
        this.googlePredicted = googlePredicted;
    }
}
