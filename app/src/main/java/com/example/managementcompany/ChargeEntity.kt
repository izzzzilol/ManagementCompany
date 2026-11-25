package com.example.managementcompany

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Nachislenie",
    foreignKeys = [
        ForeignKey(entity = ApartmentEntity::class, parentColumns = ["Id"], childColumns = ["ApartmentId"]),
        ForeignKey(entity = ServiceEntity::class, parentColumns = ["Id"], childColumns = ["ServiceId"])
    ],
    indices = [Index(value = ["ApartmentId"]), Index(value = ["ServiceId"])]
)
data class ChargeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var Id: Long = 0L,

    @ColumnInfo(name = "ApartmentId")
    var ApartmentId: Long = 0L,

    @ColumnInfo(name = "ServiceId")
    var ServiceId: Long = 0L,

    @ColumnInfo(name = "Period")
    var Period: String = "",

    @ColumnInfo(name = "Amount")
    var Amount: Double = 0.0,

    @ColumnInfo(name = "ChargeDate")
    var ChargeDate: String = "",

    @ColumnInfo(name = "PaymentStatus")
    var PaymentStatus: String = ""
)
