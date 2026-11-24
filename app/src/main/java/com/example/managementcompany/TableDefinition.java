package com.example.managementcompany;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Описание таблицы базы данных УК: имя в SQL, русское название и поля формы.
 */
public final class TableDefinition {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_REAL = "REAL";

    public static final class Column {
        public final String name;          // имя столбца в SQL
        public final String label;         // подпись в интерфейсе
        public final String type;          // TEXT / INTEGER / REAL
        public final String hint;          // подсказка в поле ввода
        public final String fkTable;       // таблица-справочник (если это внешний ключ)
        public final String fkDisplay;     // SQL-выражение для подписи справочника

        private Column(String name, String label, String type, String hint,
                       String fkTable, String fkDisplay) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.hint = hint;
            this.fkTable = fkTable;
            this.fkDisplay = fkDisplay;
        }
    }

    private final String key;
    private final String tableName;
    private final String displayName;
    private final List<Column> columns;

    private TableDefinition(String key, String tableName, String displayName, Column... columns) {
        this.key = key;
        this.tableName = tableName;
        this.displayName = displayName;
        this.columns = Collections.unmodifiableList(Arrays.asList(columns));
    }

    public String getKey() { return key; }
    public String getTableName() { return tableName; }
    public String getDisplayName() { return displayName; }
    public List<Column> getColumns() { return columns; }

    private static Column col(String name, String label, String type) {
        return new Column(name, label, type, null, null, null);
    }

    private static Column col(String name, String label, String type, String hint) {
        return new Column(name, label, type, hint, null, null);
    }

    private static Column ref(String name, String label, String fkTable, String fkDisplay) {
        return new Column(name, label, TYPE_INTEGER, null, fkTable, fkDisplay);
    }

    private static final List<TableDefinition> ALL = buildAll();

    private static List<TableDefinition> buildAll() {
        List<TableDefinition> list = new ArrayList<>();
        list.add(new TableDefinition("houses", "Houses", "Дома",
                col("Address", "Адрес", TYPE_TEXT),
                col("YearBuilt", "Год постройки", TYPE_INTEGER, "например, 2005"),
                col("Floors", "Этажность", TYPE_INTEGER, "например, 5"),
                col("Entrances", "Количество подъездов", TYPE_INTEGER, "например, 2")
        ));
        list.add(new TableDefinition("kvartira", "Kvartira", "Квартиры",
                ref("HouseId", "Дом", "Houses", "Address"),
                col("ApartmentNumber", "Номер квартиры", TYPE_INTEGER),
                col("EntranceNumber", "Подъезд", TYPE_INTEGER),
                col("Area", "Площадь, м²", TYPE_REAL, "например, 54.2"),
                ref("OwnerId", "Собственник", "Cobstveniki", "LastName || ' ' || FirstName")
        ));
        list.add(new TableDefinition("cobstveniki", "Cobstveniki", "Собственники",
                col("LastName", "Фамилия", TYPE_TEXT),
                col("FirstName", "Имя", TYPE_TEXT),
                col("MiddleName", "Отчество", TYPE_TEXT),
                col("Phone", "Телефон", TYPE_TEXT, "+7 900 000-00-00"),
                col("PassportSeries", "Серия паспорта", TYPE_TEXT),
                col("PassportNumber", "Номер паспорта", TYPE_TEXT)
        ));
        list.add(new TableDefinition("yslygi", "Yslygi", "Услуги",
                col("Name", "Наименование услуги", TYPE_TEXT),
                col("Tariff", "Тариф, руб.", TYPE_REAL, "например, 35.5")
        ));
        list.add(new TableDefinition("nachislenie", "Nachislenie", "Начисления",
                ref("ApartmentId", "Квартира", "Kvartira", "'кв. ' || ApartmentNumber"),
                ref("ServiceId", "Услуга", "Yslygi", "Name"),
                col("Period", "Период начисления", TYPE_TEXT, "ГГГГ-ММ"),
                col("Amount", "Сумма, руб.", TYPE_REAL, "например, 1920.00"),
                col("ChargeDate", "Дата начисления", TYPE_TEXT, "ГГГГ-ММ-ДД"),
                col("PaymentStatus", "Статус оплаты", TYPE_TEXT, "Оплачено / Не оплачено")
        ));
        list.add(new TableDefinition("zaivki", "Zaivki", "Заявки жильцов",
                ref("OwnerId", "Собственник", "Cobstveniki", "LastName || ' ' || FirstName"),
                col("RequestDate", "Дата заявки", TYPE_TEXT, "ГГГГ-ММ-ДД"),
                col("Description", "Описание проблемы", TYPE_TEXT),
                col("Status", "Статус", TYPE_TEXT, "Новая / В работе / Выполнено")
        ));
        list.add(new TableDefinition("rashod", "Rashod", "Расходы",
                col("ExpenseItem", "Статья расхода", TYPE_TEXT),
                col("Amount", "Сумма, руб.", TYPE_REAL, "например, 1500.00"),
                col("Date", "Дата", TYPE_TEXT, "ГГГГ-ММ-ДД"),
                ref("HouseId", "Дом", "Houses", "Address")
        ));
        return list;
    }

    public static List<TableDefinition> all() { return ALL; }

    public static TableDefinition byKey(String key) {
        if (key == null) return null;
        for (TableDefinition def : ALL) {
            if (def.key.equals(key)) return def;
        }
        return null;
    }

    public static TableDefinition byTableName(String tableName) {
        if (tableName == null) return null;
        for (TableDefinition def : ALL) {
            if (def.tableName.equals(tableName)) return def;
        }
        return null;
    }
}
