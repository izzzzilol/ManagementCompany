package com.example.managementcompany;

import java.util.Map;

/** Одна строка таблицы, загруженная из SQLite. */
public final class Record {
    private final long id;
    private final Map<String, String> values;

    Record(long id, Map<String, String> values) {
        this.id = id;
        this.values = values;
    }

    public long getId() { return id; }

    Map<String, String> getValues() { return values; }

    public String get(String column) {
        String value = values.get(column);
        return value == null ? "" : value;
    }

    /** Первые два поля — жирная строка в списке. */
    public String summary(TableDefinition def) {
        StringBuilder sb = new StringBuilder();
        int count = Math.min(2, def.getColumns().size());
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append("  ·  ");
            sb.append(get(def.getColumns().get(i).name));
        }
        return sb.toString();
    }

    /** Остальные поля — подробности под основной строкой. */
    public String details(TableDefinition def) {
        StringBuilder sb = new StringBuilder("ID ").append(id);
        for (int i = 2; i < def.getColumns().size(); i++) {
            TableDefinition.Column column = def.getColumns().get(i);
            String value = get(column.name);
            if (!value.isEmpty()) {
                sb.append("  •  ").append(column.label).append(": ").append(value);
            }
        }
        return sb.toString();
    }
}
