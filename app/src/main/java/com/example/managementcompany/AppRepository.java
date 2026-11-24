package com.example.managementcompany;

import android.content.Context;

import androidx.sqlite.db.SimpleSQLiteQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Репозиторий поверх Room. Сохраняет удобный API для экранов приложения:
 * таблицы описаны в {@link TableDefinition}, а все запросы выполняет {@link AppDao}.
 */
public class AppRepository {

    public static final String DATABASE_NAME = AppDatabase.DATABASE_NAME;

    private final AppDao dao;

    public AppRepository(Context context) {
        this.dao = AppDatabase.get(context.getApplicationContext()).dao();
    }

    /** Все записи таблицы с необязательным текстовым фильтром. */
    public List<Record> getAll(TableDefinition def, String filter) {
        List<Record> all = toRecords(def, select(def, null, null, null));
        String query = filter == null ? "" : filter.trim().toLowerCase(Locale.getDefault());
        if (query.isEmpty()) return all;

        List<Record> result = new ArrayList<>();
        for (Record record : all) {
            boolean found = false;
            for (String value : record.getValues().values()) {
                if (value.toLowerCase(Locale.getDefault()).contains(query)) {
                    found = true;
                    break;
                }
            }
            if (found) result.add(record);
        }
        return result;
    }

    /** Выборка с произвольным условием WHERE — аналог SELECT * FROM Table WHERE ... */
    public List<Record> getRecordsWhere(TableDefinition def, String where,
                                        String[] args, String orderBy) {
        return toRecords(def, select(def, where, args, orderBy));
    }

    public Record getRecordById(TableDefinition def, long id) {
        List<Record> found = getRecordsWhere(def, "Id=?",
                new String[]{String.valueOf(id)}, null);
        return found.isEmpty() ? null : found.get(0);
    }

    public int count(TableDefinition def) {
        return dao.selectInt(new SimpleSQLiteQuery(
                "SELECT COUNT(*) FROM " + def.getTableName()));
    }

    public int countWhere(String table, String where, String[] args) {
        String sql = "SELECT COUNT(*) FROM " + table;
        if (where != null && !where.isEmpty()) sql += " WHERE " + where;
        return dao.selectInt(new SimpleSQLiteQuery(sql, args));
    }

    public double sumWhere(String table, String column, String where) {
        String sql = "SELECT COALESCE(SUM(" + column + "), 0) FROM " + table;
        if (where != null && !where.isEmpty()) sql += " WHERE " + where;
        return dao.selectDouble(new SimpleSQLiteQuery(sql));
    }

    /** Квартиры собственника. */
    public List<Record> getApartmentsByOwner(long ownerId) {
        return getRecordsWhere(TableDefinition.byKey("kvartira"), "OwnerId=?",
                new String[]{String.valueOf(ownerId)}, "ApartmentNumber");
    }

    /** Заявки собственника. */
    public List<Record> getRequestsByOwner(long ownerId) {
        return getRecordsWhere(TableDefinition.byKey("zaivki"), "OwnerId=?",
                new String[]{String.valueOf(ownerId)}, "RequestDate DESC");
    }

    /** Начисления по квартирам собственника. */
    public List<Record> getChargesByOwner(long ownerId) {
        return getRecordsWhere(TableDefinition.byKey("nachislenie"),
                "ApartmentId IN (SELECT Id FROM Kvartira WHERE OwnerId=?)",
                new String[]{String.valueOf(ownerId)}, "Period DESC");
    }

    /** Справочник для выпадающего списка внешнего ключа. */
    public List<RefItem> getReferenceItems(String table, String displayExpression) {
        String sql = "SELECT Id, (" + displayExpression + ") AS label FROM " + table
                + " ORDER BY label";
        List<RefRow> rows = dao.selectRefRows(new SimpleSQLiteQuery(sql));
        List<RefItem> result = new ArrayList<>();
        for (RefRow row : rows) {
            result.add(new RefItem(row.Id, row.label == null ? "" : row.label));
        }
        return result;
    }

    public long insert(TableDefinition def, Map<String, String> values) {
        Object entity = buildEntity(def, values);
        switch (def.getKey()) {
            case "houses": return dao.insertHouse((HouseEntity) entity);
            case "kvartira": return dao.insertApartment((ApartmentEntity) entity);
            case "cobstveniki": return dao.insertOwner((OwnerEntity) entity);
            case "yslygi": return dao.insertService((ServiceEntity) entity);
            case "nachislenie": return dao.insertCharge((ChargeEntity) entity);
            case "zaivki": return dao.insertRequest((RequestEntity) entity);
            case "rashod": return dao.insertExpense((ExpenseEntity) entity);
            default: throw new IllegalArgumentException("Неизвестная таблица");
        }
    }

    public int update(TableDefinition def, long id, Map<String, String> values) {
        Map<String, String> withId = new LinkedHashMap<>(values);
        withId.put("Id", String.valueOf(id));
        Object entity = buildEntity(def, withId);
        switch (def.getKey()) {
            case "houses": return dao.updateHouse((HouseEntity) entity);
            case "kvartira": return dao.updateApartment((ApartmentEntity) entity);
            case "cobstveniki": return dao.updateOwner((OwnerEntity) entity);
            case "yslygi": return dao.updateService((ServiceEntity) entity);
            case "nachislenie": return dao.updateCharge((ChargeEntity) entity);
            case "zaivki": return dao.updateRequest((RequestEntity) entity);
            case "rashod": return dao.updateExpense((ExpenseEntity) entity);
            default: throw new IllegalArgumentException("Неизвестная таблица");
        }
    }

    public void delete(TableDefinition def, long id) {
        Object entity = newEntity(def);
        setId(entity, id);
        switch (def.getKey()) {
            case "houses": dao.deleteHouse((HouseEntity) entity); break;
            case "kvartira": dao.deleteApartment((ApartmentEntity) entity); break;
            case "cobstveniki": dao.deleteOwner((OwnerEntity) entity); break;
            case "yslygi": dao.deleteService((ServiceEntity) entity); break;
            case "nachislenie": dao.deleteCharge((ChargeEntity) entity); break;
            case "zaivki": dao.deleteRequest((RequestEntity) entity); break;
            case "rashod": dao.deleteExpense((ExpenseEntity) entity); break;
            default: throw new IllegalArgumentException("Неизвестная таблица");
        }
    }

    /** Room живёт всё время работы приложения, поэтому закрывать его не нужно. */
    public void close() { }

    private List<?> select(TableDefinition def, String where, String[] args, String orderBy) {
        String sql = "SELECT * FROM " + def.getTableName();
        if (where != null && !where.isEmpty()) sql += " WHERE " + where;
        sql += " ORDER BY " + (orderBy == null ? "Id" : orderBy);
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(sql, args);

        switch (def.getKey()) {
            case "houses": return dao.selectHouses(query);
            case "kvartira": return dao.selectApartments(query);
            case "cobstveniki": return dao.selectOwners(query);
            case "yslygi": return dao.selectServices(query);
            case "nachislenie": return dao.selectCharges(query);
            case "zaivki": return dao.selectRequests(query);
            case "rashod": return dao.selectExpenses(query);
            default: return Collections.emptyList();
        }
    }

    private List<Record> toRecords(TableDefinition def, List<?> entities) {
        List<Record> result = new ArrayList<>();
        for (Object entity : entities) result.add(toRecord(def, entity));
        return result;
    }

    private Record toRecord(TableDefinition def, Object entity) {
        Map<String, String> values = new LinkedHashMap<>();
        try {
            for (TableDefinition.Column column : def.getColumns()) {
                Object value = entity.getClass().getField(column.name).get(entity);
                values.put(column.name, value == null ? "" : String.valueOf(value));
            }
            long id = ((Number) entity.getClass().getField("Id").get(entity)).longValue();
            return new Record(id, values);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось прочитать запись", e);
        }
    }

    private Object buildEntity(TableDefinition def, Map<String, String> values) {
        Object entity = newEntity(def);
        try {
            String rawId = values.get("Id");
            if (rawId != null && !rawId.trim().isEmpty()) {
                setId(entity, Long.parseLong(rawId.trim()));
            }
            for (TableDefinition.Column column : def.getColumns()) {
                Field field = entity.getClass().getField(column.name);
                field.set(entity, parse(values.get(column.name), field.getType(), column));
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось подготовить запись", e);
        }
        return entity;
    }

    private Object parse(String raw, Class<?> type, TableDefinition.Column column) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            if (type == String.class) return "";
            if (type == int.class || type == Integer.class) return 0;
            if (type == long.class || type == Long.class) return 0L;
            if (type == double.class || type == Double.class) return 0.0;
            return null;
        }
        try {
            if (type == String.class) return value;
            if (type == int.class || type == Integer.class) return Integer.parseInt(value);
            if (type == long.class || type == Long.class) return Long.parseLong(value);
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(value.replace(',', '.'));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Поле «" + column.label + "» должно быть числом", e);
        }
    }

    private Object newEntity(TableDefinition def) {
        switch (def.getKey()) {
            case "houses": return new HouseEntity();
            case "kvartira": return new ApartmentEntity();
            case "cobstveniki": return new OwnerEntity();
            case "yslygi": return new ServiceEntity();
            case "nachislenie": return new ChargeEntity();
            case "zaivki": return new RequestEntity();
            case "rashod": return new ExpenseEntity();
            default: throw new IllegalArgumentException("Неизвестная таблица");
        }
    }

    private void setId(Object entity, long id) {
        try {
            entity.getClass().getField("Id").setLong(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не удалось задать идентификатор", e);
        }
    }
}
