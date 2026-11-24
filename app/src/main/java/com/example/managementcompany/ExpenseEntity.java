package com.example.managementcompany;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Rashod",
        foreignKeys = @ForeignKey(entity = HouseEntity.class, parentColumns = "Id", childColumns = "HouseId"),
        indices = {@Index("HouseId")})
public class ExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    public long Id;

    @ColumnInfo(name = "ExpenseItem")
    public String ExpenseItem;

    @ColumnInfo(name = "Amount")
    public double Amount;

    @ColumnInfo(name = "Date")
    public String Date;

    @ColumnInfo(name = "HouseId")
    public long HouseId;
}
