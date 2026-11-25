package com.example.managementcompany

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Cobstveniki")
data class OwnerEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var Id: Long = 0L,

    @ColumnInfo(name = "LastName")
    var LastName: String = "",

    @ColumnInfo(name = "FirstName")
    var FirstName: String = "",

    @ColumnInfo(name = "MiddleName")
    var MiddleName: String = "",

    @ColumnInfo(name = "Phone")
    var Phone: String = "",

    @ColumnInfo(name = "PassportSeries")
    var PassportSeries: String = "",

    @ColumnInfo(name = "PassportNumber")
    var PassportNumber: String = ""
)
