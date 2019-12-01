package com.example.batterylifepredection.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.batterylifepredection.entities.AllData;
import com.example.batterylifepredection.entities.TemporaryDatabase;

import java.util.List;

@Dao
public interface DatabaseDao {

    @Query("SELECT * FROM tempdata")
    LiveData <List <TemporaryDatabase>> getAll();
    @Query("SELECT * FROM tempdata WHERE battery_level = :level")
    LiveData <List <TemporaryDatabase>> getRecord(int level);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TemporaryDatabase data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AllData data);

    @Query("DELETE FROM tempdata")
    void deteleAll();
}