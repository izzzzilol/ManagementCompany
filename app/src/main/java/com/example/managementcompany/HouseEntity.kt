package com.example.managementcompany

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Houses")
data class HouseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var Id: Long = 0L,

    @ColumnInfo(name = "Address")
    var Address: String = "",

    @ColumnInfo(name = "YearBuilt")
    var YearBuilt: Int = 0,

    @ColumnInfo(name = "Floors")
    var Floors: Int = 0,

    @ColumnInfo(name = "Entrances")
    var Entrances: Int = 0
)
