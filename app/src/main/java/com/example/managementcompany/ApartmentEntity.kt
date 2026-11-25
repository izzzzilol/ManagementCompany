package com.example.managementcompany

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Kvartira",
    foreignKeys = [
        ForeignKey(entity = HouseEntity::class, parentColumns = ["Id"], childColumns = ["HouseId"]),
        ForeignKey(entity = OwnerEntity::class, parentColumns = ["Id"], childColumns = ["OwnerId"])
    ],
    indices = [Index(value = ["HouseId"]), Index(value = ["OwnerId"])]
)
data class ApartmentEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var Id: Long = 0L,

    @ColumnInfo(name = "HouseId")
    var HouseId: Long = 0L,

    @ColumnInfo(name = "ApartmentNumber")
    var ApartmentNumber: Int = 0,

    @ColumnInfo(name = "EntranceNumber")
    var EntranceNumber: Int = 0,

    @ColumnInfo(name = "Area")
    var Area: Double = 0.0,

    @ColumnInfo(name = "OwnerId")
    var OwnerId: Long = 0L
)
