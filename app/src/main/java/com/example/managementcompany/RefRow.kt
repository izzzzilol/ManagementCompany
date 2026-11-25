package com.example.managementcompany

import androidx.room.ColumnInfo

/** Строка справочника: идентификатор и отображаемое название. */
data class RefRow(
    @ColumnInfo(name = "Id")
    val Id: Long = 0L,

    @ColumnInfo(name = "label")
    val label: String? = null
)
