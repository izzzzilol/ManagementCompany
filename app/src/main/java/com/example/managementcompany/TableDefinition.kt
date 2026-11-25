package com.example.managementcompany

/**
 * Описание таблицы базы данных УК: имя в SQL, русское название и поля формы.
 */
object TableDefinition {

    const val TYPE_TEXT = "TEXT"
    const val TYPE_INTEGER = "INTEGER"
    const val TYPE_REAL = "REAL"

    data class Column(
        val name: String,       // имя столбца в SQL
        val label: String,      // подпись в интерфейсе
        val type: String,       // TEXT / INTEGER / REAL
        val hint: String?,      // подсказка в поле ввода
        val fkTable: String?,   // таблица-справочник (если это внешний ключ)
        val fkDisplay: String?  // SQL-выражение для подписи справочника
    )

    /** Одна таблица: ключ, имя в SQL, русское название и поля формы. */
    data class TableDefinition(
        val key: String,
        val tableName: String,
        val displayName: String,
        val columns: List<Column>,
    )

    private fun col(name: String, label: String, type: String, hint: String? = null): Column =
        Column(name, label, type, hint, null, null)

    private fun ref(name: String, label: String, fkTable: String, fkDisplay: String): Column =
        Column(name, label, TYPE_INTEGER, null, fkTable, fkDisplay)

    private val definitions: List<TableDefinition> = listOf(
        TableDefinition(
            key = "houses",
            tableName = "Houses",
            displayName = "Дома",
            columns = listOf(
                col("Address", "Адрес", TYPE_TEXT),
                col("YearBuilt", "Год постройки", TYPE_INTEGER, "например, 2005"),
                col("Floors", "Этажность", TYPE_INTEGER, "например, 5"),
                col("Entrances", "Количество подъездов", TYPE_INTEGER, "например, 2"),
            ),
        ),
        TableDefinition(
            key = "kvartira",
            tableName = "Kvartira",
            displayName = "Квартиры",
            columns = listOf(
                ref("HouseId", "Дом", "Houses", "Address"),
                col("ApartmentNumber", "Номер квартиры", TYPE_INTEGER),
                col("EntranceNumber", "Подъезд", TYPE_INTEGER),
                col("Area", "Площадь, м²", TYPE_REAL, "например, 54.2"),
                ref("OwnerId", "Собственник", "Cobstveniki", "LastName || ' ' || FirstName"),
            ),
        ),
        TableDefinition(
            key = "cobstveniki",
            tableName = "Cobstveniki",
            displayName = "Собственники",
            columns = listOf(
                col("LastName", "Фамилия", TYPE_TEXT),
                col("FirstName", "Имя", TYPE_TEXT),
                col("MiddleName", "Отчество", TYPE_TEXT),
                col("Phone", "Телефон", TYPE_TEXT, "+7 900 000-00-00"),
                col("PassportSeries", "Серия паспорта", TYPE_TEXT),
                col("PassportNumber", "Номер паспорта", TYPE_TEXT),
            ),
        ),
        TableDefinition(
            key = "yslygi",
            tableName = "Yslygi",
            displayName = "Услуги",
            columns = listOf(
                col("Name", "Наименование услуги", TYPE_TEXT),
                col("Tariff", "Тариф, руб.", TYPE_REAL, "например, 35.5"),
            ),
        ),
        TableDefinition(
            key = "nachislenie",
            tableName = "Nachislenie",
            displayName = "Начисления",
            columns = listOf(
                ref("ApartmentId", "Квартира", "Kvartira", "'кв. ' || ApartmentNumber"),
                ref("ServiceId", "Услуга", "Yslygi", "Name"),
                col("Period", "Период начисления", TYPE_TEXT, "ГГГГ-ММ"),
                col("Amount", "Сумма, руб.", TYPE_REAL, "например, 1920.00"),
                col("ChargeDate", "Дата начисления", TYPE_TEXT, "ГГГГ-ММ-ДД"),
                col("PaymentStatus", "Статус оплаты", TYPE_TEXT, "Оплачено / Не оплачено"),
            ),
        ),
        TableDefinition(
            key = "zaivki",
            tableName = "Zaivki",
            displayName = "Заявки жильцов",
            columns = listOf(
                ref("OwnerId", "Собственник", "Cobstveniki", "LastName || ' ' || FirstName"),
                col("RequestDate", "Дата заявки", TYPE_TEXT, "ГГГГ-ММ-ДД"),
                col("Description", "Описание проблемы", TYPE_TEXT),
                col("Status", "Статус", TYPE_TEXT, "Новая / В работе / Выполнено"),
            ),
        ),
        TableDefinition(
            key = "rashod",
            tableName = "Rashod",
            displayName = "Расходы",
            columns = listOf(
                col("ExpenseItem", "Статья расхода", TYPE_TEXT),
                col("Amount", "Сумма, руб.", TYPE_REAL, "например, 1500.00"),
                col("Date", "Дата", TYPE_TEXT, "ГГГГ-ММ-ДД"),
                ref("HouseId", "Дом", "Houses", "Address"),
            ),
        ),
    )

    @JvmStatic
    fun all(): List<TableDefinition> = definitions

    @JvmStatic
    fun byKey(key: String?): TableDefinition? =
        key?.let { value -> definitions.firstOrNull { it.key == value } }

    @JvmStatic
    fun byTableName(tableName: String?): TableDefinition? =
        tableName?.let { value -> definitions.firstOrNull { it.tableName == value } }
}
