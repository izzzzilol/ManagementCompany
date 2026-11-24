package com.example.managementcompany;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Zaivki",
        foreignKeys = @ForeignKey(entity = OwnerEntity.class, parentColumns = "Id", childColumns = "OwnerId"),
        indices = {@Index("OwnerId")})
public class RequestEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    public long Id;

    @ColumnInfo(name = "OwnerId")
    public long OwnerId;

    @ColumnInfo(name = "RequestDate")
    public String RequestDate;

    @ColumnInfo(name = "Description")
    public String Description;

    @ColumnInfo(name = "Status")
    public String Status;
}
