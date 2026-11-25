package com.example.managementcompany

import com.example.managementcompany.TableDefinition.TableDefinition
import java.util.LinkedHashMap

/** Одна строка таблицы, загруженная из SQLite. */
class Record(
    val id: Long,
    val values: LinkedHashMap<String, String>
) {
    operator fun get(name: String): String = values[name] ?: ""

    /** Первые два поля — жирная строка в списке. */
    fun summary(def: TableDefinition): String = buildString {
        val count = minOf(2, def.columns.size)
        for (i in 0 until count) {
            if (i > 0) append("  ·  ")
            append(get(def.columns[i].name))
        }
    }

    /** Остальные поля — подробности под основной строкой. */
    fun details(def: TableDefinition): String = buildString {
        append("ID ").append(id)
        for (i in 2 until def.columns.size) {
            val column = def.columns[i]
            val value = get(column.name)
            if (value.isNotEmpty()) {
                append("  •  ").append(column.label).append(": ").append(value)
            }
        }
    }
}
