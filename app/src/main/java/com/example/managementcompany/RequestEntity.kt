package com.example.managementcompany

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Zaivki",
    foreignKeys = [
        ForeignKey(entity = OwnerEntity::class, parentColumns = ["Id"], childColumns = ["OwnerId"])
    ],
    indices = [Index(value = ["OwnerId"])]
)
data class RequestEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var Id: Long = 0L,

    @ColumnInfo(name = "OwnerId")
    var OwnerId: Long = 0L,

    @ColumnInfo(name = "RequestDate")
    var RequestDate: String = "",

    @ColumnInfo(name = "Description")
    var Description: String = "",

    @ColumnInfo(name = "Status")
    var Status: String = ""
)
