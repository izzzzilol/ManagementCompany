package com.example.managementcompany

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Yslygi")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var Id: Long = 0L,

    @ColumnInfo(name = "Name")
    var Name: String = "",

    @ColumnInfo(name = "Tariff")
    var Tariff: Double = 0.0
)
