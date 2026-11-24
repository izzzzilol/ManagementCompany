package com.example.managementcompany;

import androidx.room.ColumnInfo;

/** Строка справочника: идентификатор и отображаемое название. */
public class RefRow {

    @ColumnInfo(name = "Id")
    public long Id;

    @ColumnInfo(name = "label")
    public String label;
}
