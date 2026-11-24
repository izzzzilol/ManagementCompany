package com.example.managementcompany;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Nachislenie",
        foreignKeys = {
                @ForeignKey(entity = ApartmentEntity.class, parentColumns = "Id", childColumns = "ApartmentId"),
                @ForeignKey(entity = ServiceEntity.class, parentColumns = "Id", childColumns = "ServiceId")
        },
        indices = {@Index("ApartmentId"), @Index("ServiceId")})
public class ChargeEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    public long Id;

    @ColumnInfo(name = "ApartmentId")
    public long ApartmentId;

    @ColumnInfo(name = "ServiceId")
    public long ServiceId;

    @ColumnInfo(name = "Period")
    public String Period;

    @ColumnInfo(name = "Amount")
    public double Amount;

    @ColumnInfo(name = "ChargeDate")
    public String ChargeDate;

    @ColumnInfo(name = "PaymentStatus")
    public String PaymentStatus;
}
