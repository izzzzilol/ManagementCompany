package com.example.managementcompany;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Cobstveniki")
public class OwnerEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    public long Id;

    @ColumnInfo(name = "LastName")
    public String LastName;

    @ColumnInfo(name = "FirstName")
    public String FirstName;

    @ColumnInfo(name = "MiddleName")
    public String MiddleName;

    @ColumnInfo(name = "Phone")
    public String Phone;

    @ColumnInfo(name = "PassportSeries")
    public String PassportSeries;

    @ColumnInfo(name = "PassportNumber")
    public String PassportNumber;
}
