package com.example.managementcompany;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Kvartira",
        foreignKeys = {
                @ForeignKey(entity = HouseEntity.class, parentColumns = "Id", childColumns = "HouseId"),
                @ForeignKey(entity = OwnerEntity.class, parentColumns = "Id", childColumns = "OwnerId")
        },
        indices = {@Index("HouseId"), @Index("OwnerId")})
public class ApartmentEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    public long Id;

    @ColumnInfo(name = "HouseId")
    public long HouseId;

    @ColumnInfo(name = "ApartmentNumber")
    public int ApartmentNumber;

    @ColumnInfo(name = "EntranceNumber")
    public int EntranceNumber;

    @ColumnInfo(name = "Area")
    public double Area;

    @ColumnInfo(name = "OwnerId")
    public long OwnerId;
}
