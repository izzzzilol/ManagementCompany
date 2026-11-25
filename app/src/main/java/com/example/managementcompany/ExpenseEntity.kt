package com.example.managementcompany

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Rashod",
    foreignKeys = [
        ForeignKey(entity = HouseEntity::class, parentColumns = ["Id"], childColumns = ["HouseId"])
    ],
    indices = [Index(value = ["HouseId"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id")
    var Id: Long = 0L,

    @ColumnInfo(name = "ExpenseItem")
    var ExpenseItem: String = "",

    @ColumnInfo(name = "Amount")
    var Amount: Double = 0.0,

    @ColumnInfo(name = "Date")
    var Date: String = "",

    @ColumnInfo(name = "HouseId")
    var HouseId: Long = 0L
)
