package com.example.batterylifepredection.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "data")
public class AllData {
    @PrimaryKey(autoGenerate = true)
    private int uid;

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    @ColumnInfo(name = "current_time")
    public String currentTime;

    @ColumnInfo(name = "my_predicted")
    private String myPredicted;

    @ColumnInfo(name = "samsung_predicted")
    private String samsungPredicted;


    @ColumnInfo(name = "battery_level")
    public int batteryLevel;
    public AllData(){

    }
    public AllData(TemporaryDatabase tempdb){
        this.currentTime = tempdb.currentTime;
        this.myPredicted = tempdb.myPredicted;
        this.samsungPredicted = tempdb.googlePredicted;
        this.batteryLevel = tempdb.batteryLevel;

    }
    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
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

    public String getSamsungPredicted() {
        return samsungPredicted;
    }

    public void setSamsungPredicted(String samsungPredicted) {
        this.samsungPredicted = samsungPredicted;
    }
}
