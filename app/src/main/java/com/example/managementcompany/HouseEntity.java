package com.example.managementcompany;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Houses")
public class HouseEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    public long Id;

    @ColumnInfo(name = "Address")
    public String Address;

    @ColumnInfo(name = "YearBuilt")
    public Integer YearBuilt;

    @ColumnInfo(name = "Floors")
    public Integer Floors;

    @ColumnInfo(name = "Entrances")
    public Integer Entrances;
}
