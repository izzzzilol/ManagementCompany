package com.example.managementcompany

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.Column
import com.example.managementcompany.TableDefinition.TableDefinition
import java.util.Locale

/**
 * Репозиторий поверх Room. Сохраняет удобный API для экранов приложения:
 * таблицы описаны в [TableDefinition], а все запросы выполняет [AppDao].
 */
class AppRepository(context: Context) {

    private val dao: AppDao = AppDatabase.get(context.applicationContext).dao()

    companion object {
        const val DATABASE_NAME = AppDatabase.DATABASE_NAME
    }

    /** Все записи таблицы с необязательным текстовым фильтром. */
    fun getAll(def: TableDefinition, filter: String?): List<Record> {
        val all = toRecords(def, select(def, null, null, null))
        val query = filter?.trim()?.lowercase(Locale.getDefault()).orEmpty()
        if (query.isEmpty()) return all

        return all.filter { record ->
            record.values.values.any { it.lowercase(Locale.getDefault()).contains(query) }
        }
    }

    /** Выборка с произвольным условием WHERE — аналог SELECT * FROM Table WHERE ... */
    fun getRecordsWhere(
        def: TableDefinition,
        where: String?,
        args: Array<String>?,
        orderBy: String?
    ): List<Record> = toRecords(def, select(def, where, args, orderBy))

    fun getRecordById(def: TableDefinition, id: Long): Record? =
        getRecordsWhere(def, "Id=?", arrayOf(id.toString()), null).firstOrNull()

    fun count(def: TableDefinition): Int =
        dao.selectInt(SimpleSQLiteQuery("SELECT COUNT(*) FROM ${def.tableName}"))

    fun countWhere(table: String, where: String?, args: Array<String>?): Int {
        var sql = "SELECT COUNT(*) FROM $table"
        if (!where.isNullOrEmpty()) sql += " WHERE $where"
        return dao.selectInt(SimpleSQLiteQuery(sql, args))
    }

    fun sumWhere(table: String, column: String, where: String?): Double {
        var sql = "SELECT COALESCE(SUM($column), 0) FROM $table"
        if (!where.isNullOrEmpty()) sql += " WHERE $where"
        return dao.selectDouble(SimpleSQLiteQuery(sql))
    }

    /** Квартиры собственника. */
    fun getApartmentsByOwner(ownerId: Long): List<Record> {
        val def = Definitions.byKey("kvartira") ?: return emptyList()
        return getRecordsWhere(def, "OwnerId=?", arrayOf(ownerId.toString()), "ApartmentNumber")
    }

    /** Заявки собственника. */
    fun getRequestsByOwner(ownerId: Long): List<Record> {
        val def = Definitions.byKey("zaivki") ?: return emptyList()
        return getRecordsWhere(def, "OwnerId=?", arrayOf(ownerId.toString()), "RequestDate DESC")
    }

    /** Начисления по квартирам собственника. */
    fun getChargesByOwner(ownerId: Long): List<Record> {
        val def = Definitions.byKey("nachislenie") ?: return emptyList()
        return getRecordsWhere(
            def,
            "ApartmentId IN (SELECT Id FROM Kvartira WHERE OwnerId=?)",
            arrayOf(ownerId.toString()),
            "Period DESC"
        )
    }

    /** Справочник для выпадающего списка внешнего ключа. */
    fun getReferenceItems(table: String, displayExpression: String): List<RefItem> {
        val sql = "SELECT Id, ($displayExpression) AS label FROM $table ORDER BY label"
        return dao.selectRefRows(SimpleSQLiteQuery(sql))
            .map { row -> RefItem(row.Id, row.label ?: "") }
    }

    fun insert(def: TableDefinition, values: Map<String, String>): Long =
        when (val entity = buildEntity(def, values)) {
            is HouseEntity -> dao.insertHouse(entity)
            is ApartmentEntity -> dao.insertApartment(entity)
            is OwnerEntity -> dao.insertOwner(entity)
            is ServiceEntity -> dao.insertService(entity)
            is ChargeEntity -> dao.insertCharge(entity)
            is RequestEntity -> dao.insertRequest(entity)
            is ExpenseEntity -> dao.insertExpense(entity)
            else -> throw IllegalArgumentException("Неизвестная таблица")
        }

    fun update(def: TableDefinition, id: Long, values: Map<String, String>): Int {
        val withId = LinkedHashMap(values)
        withId["Id"] = id.toString()
        return when (val entity = buildEntity(def, withId)) {
            is HouseEntity -> dao.updateHouse(entity)
            is ApartmentEntity -> dao.updateApartment(entity)
            is OwnerEntity -> dao.updateOwner(entity)
            is ServiceEntity -> dao.updateService(entity)
            is ChargeEntity -> dao.updateCharge(entity)
            is RequestEntity -> dao.updateRequest(entity)
            is ExpenseEntity -> dao.updateExpense(entity)
            else -> throw IllegalArgumentException("Неизвестная таблица")
        }
    }

    fun delete(def: TableDefinition, id: Long) {
        val entity = newEntity(def)
        setId(entity, id)
        when (entity) {
            is HouseEntity -> dao.deleteHouse(entity)
            is ApartmentEntity -> dao.deleteApartment(entity)
            is OwnerEntity -> dao.deleteOwner(entity)
            is ServiceEntity -> dao.deleteService(entity)
            is ChargeEntity -> dao.deleteCharge(entity)
            is RequestEntity -> dao.deleteRequest(entity)
            is ExpenseEntity -> dao.deleteExpense(entity)
            else -> throw IllegalArgumentException("Неизвестная таблица")
        }
    }

    /** Смена статуса выполняется в транзакции Room. */
    fun setStatus(def: TableDefinition, id: Long, column: String, value: String): Int {
        if (def.key == "zaivki" && column == "Status") {
            return dao.setRequestStatus(id, value)
        }
        if (def.key == "nachislenie" && column == "PaymentStatus") {
            return dao.setChargeStatus(id, value)
        }
        return update(def, id, mapOf(column to value))
    }

    /** Room живёт всё время работы приложения, поэтому закрывать его не нужно. */
    fun close() { }

    private fun select(
        def: TableDefinition,
        where: String?,
        args: Array<String>?,
        orderBy: String?
    ): List<Any> {
        var sql = "SELECT * FROM ${def.tableName}"
        if (!where.isNullOrEmpty()) sql += " WHERE $where"
        sql += " ORDER BY ${orderBy ?: "Id"}"
        val query = SimpleSQLiteQuery(sql, args)

        return when (def.key) {
            "houses" -> dao.selectHouses(query)
            "kvartira" -> dao.selectApartments(query)
            "cobstveniki" -> dao.selectOwners(query)
            "yslygi" -> dao.selectServices(query)
            "nachislenie" -> dao.selectCharges(query)
            "zaivki" -> dao.selectRequests(query)
            "rashod" -> dao.selectExpenses(query)
            else -> emptyList()
        }
    }

    private fun toRecords(def: TableDefinition, entities: List<Any>): List<Record> =
        entities.map { entity -> toRecord(def, entity) }

    private fun toRecord(def: TableDefinition, entity: Any): Record {
        val values = LinkedHashMap<String, String>()
        for (column in def.columns) {
            values[column.name] = read(entity, column.name)?.toString() ?: ""
        }
        return Record(idOf(entity), values)
    }

    /** Чтение поля сущности без рефлексии. */
    private fun read(entity: Any, name: String): Any? = when (entity) {
        is HouseEntity -> when (name) {
            "Id" -> entity.Id
            "Address" -> entity.Address
            "YearBuilt" -> entity.YearBuilt
            "Floors" -> entity.Floors
            "Entrances" -> entity.Entrances
            else -> null
        }
        is ApartmentEntity -> when (name) {
            "Id" -> entity.Id
            "HouseId" -> entity.HouseId
            "ApartmentNumber" -> entity.ApartmentNumber
            "EntranceNumber" -> entity.EntranceNumber
            "Area" -> entity.Area
            "OwnerId" -> entity.OwnerId
            else -> null
        }
        is OwnerEntity -> when (name) {
            "Id" -> entity.Id
            "LastName" -> entity.LastName
            "FirstName" -> entity.FirstName
            "MiddleName" -> entity.MiddleName
            "Phone" -> entity.Phone
            "PassportSeries" -> entity.PassportSeries
            "PassportNumber" -> entity.PassportNumber
            else -> null
        }
        is ServiceEntity -> when (name) {
            "Id" -> entity.Id
            "Name" -> entity.Name
            "Tariff" -> entity.Tariff
            else -> null
        }
        is ChargeEntity -> when (name) {
            "Id" -> entity.Id
            "ApartmentId" -> entity.ApartmentId
            "ServiceId" -> entity.ServiceId
            "Period" -> entity.Period
            "Amount" -> entity.Amount
            "ChargeDate" -> entity.ChargeDate
            "PaymentStatus" -> entity.PaymentStatus
            else -> null
        }
        is RequestEntity -> when (name) {
            "Id" -> entity.Id
            "OwnerId" -> entity.OwnerId
            "RequestDate" -> entity.RequestDate
            "Description" -> entity.Description
            "Status" -> entity.Status
            else -> null
        }
        is ExpenseEntity -> when (name) {
            "Id" -> entity.Id
            "ExpenseItem" -> entity.ExpenseItem
            "Amount" -> entity.Amount
            "Date" -> entity.Date
            "HouseId" -> entity.HouseId
            else -> null
        }
        else -> null
    }

    private fun idOf(entity: Any): Long = when (entity) {
        is HouseEntity -> entity.Id
        is ApartmentEntity -> entity.Id
        is OwnerEntity -> entity.Id
        is ServiceEntity -> entity.Id
        is ChargeEntity -> entity.Id
        is RequestEntity -> entity.Id
        is ExpenseEntity -> entity.Id
        else -> throw IllegalStateException("Не удалось прочитать запись")
    }

    private fun buildEntity(def: TableDefinition, values: Map<String, String>): Any {
        val entity = newEntity(def)
        val rawId = values["Id"]
        if (!rawId.isNullOrBlank()) {
            setId(entity, rawId.trim().toLong())
        }
        when (entity) {
            is HouseEntity -> entity.fill(def, values)
            is ApartmentEntity -> entity.fill(def, values)
            is OwnerEntity -> entity.fill(def, values)
            is ServiceEntity -> entity.fill(def, values)
            is ChargeEntity -> entity.fill(def, values)
            is RequestEntity -> entity.fill(def, values)
            is ExpenseEntity -> entity.fill(def, values)
            else -> throw IllegalStateException("Не удалось подготовить запись")
        }
        return entity
    }

    private fun newEntity(def: TableDefinition): Any = when (def.key) {
        "houses" -> HouseEntity()
        "kvartira" -> ApartmentEntity()
        "cobstveniki" -> OwnerEntity()
        "yslygi" -> ServiceEntity()
        "nachislenie" -> ChargeEntity()
        "zaivki" -> RequestEntity()
        "rashod" -> ExpenseEntity()
        else -> throw IllegalArgumentException("Неизвестная таблица")
    }

    private fun setId(entity: Any, id: Long) {
        when (entity) {
            is HouseEntity -> entity.Id = id
            is ApartmentEntity -> entity.Id = id
            is OwnerEntity -> entity.Id = id
            is ServiceEntity -> entity.Id = id
            is ChargeEntity -> entity.Id = id
            is RequestEntity -> entity.Id = id
            is ExpenseEntity -> entity.Id = id
            else -> throw IllegalStateException("Не удалось задать идентификатор")
        }
    }

    private fun HouseEntity.fill(def: TableDefinition, values: Map<String, String>) {
        for (column in def.columns) {
            when (column.name) {
                "Address" -> Address = text(values[column.name])
                "YearBuilt" -> YearBuilt = integer(column, values[column.name])
                "Floors" -> Floors = integer(column, values[column.name])
                "Entrances" -> Entrances = integer(column, values[column.name])
            }
        }
    }

    private fun ApartmentEntity.fill(def: TableDefinition, values: Map<String, String>) {
        for (column in def.columns) {
            when (column.name) {
                "HouseId" -> HouseId = long(column, values[column.name])
                "ApartmentNumber" -> ApartmentNumber = integer(column, values[column.name])
                "EntranceNumber" -> EntranceNumber = integer(column, values[column.name])
                "Area" -> Area = real(column, values[column.name])
                "OwnerId" -> OwnerId = long(column, values[column.name])
            }
        }
    }

    private fun OwnerEntity.fill(def: TableDefinition, values: Map<String, String>) {
        for (column in def.columns) {
            when (column.name) {
                "LastName" -> LastName = text(values[column.name])
                "FirstName" -> FirstName = text(values[column.name])
                "MiddleName" -> MiddleName = text(values[column.name])
                "Phone" -> Phone = text(values[column.name])
                "PassportSeries" -> PassportSeries = text(values[column.name])
                "PassportNumber" -> PassportNumber = text(values[column.name])
            }
        }
    }

    private fun ServiceEntity.fill(def: TableDefinition, values: Map<String, String>) {
        for (column in def.columns) {
            when (column.name) {
                "Name" -> Name = text(values[column.name])
                "Tariff" -> Tariff = real(column, values[column.name])
            }
        }
    }

    private fun ChargeEntity.fill(def: TableDefinition, values: Map<String, String>) {
        for (column in def.columns) {
            when (column.name) {
                "ApartmentId" -> ApartmentId = long(column, values[column.name])
                "ServiceId" -> ServiceId = long(column, values[column.name])
                "Period" -> Period = text(values[column.name])
                "Amount" -> Amount = real(column, values[column.name])
                "ChargeDate" -> ChargeDate = text(values[column.name])
                "PaymentStatus" -> PaymentStatus = text(values[column.name])
            }
        }
    }

    private fun RequestEntity.fill(def: TableDefinition, values: Map<String, String>) {
        for (column in def.columns) {
            when (column.name) {
                "OwnerId" -> OwnerId = long(column, values[column.name])
                "RequestDate" -> RequestDate = text(values[column.name])
                "Description" -> Description = text(values[column.name])
                "Status" -> Status = text(values[column.name])
            }
        }
    }

    private fun ExpenseEntity.fill(def: TableDefinition, values: Map<String, String>) {
        for (column in def.columns) {
            when (column.name) {
                "ExpenseItem" -> ExpenseItem = text(values[column.name])
                "Amount" -> Amount = real(column, values[column.name])
                "Date" -> Date = text(values[column.name])
                "HouseId" -> HouseId = long(column, values[column.name])
            }
        }
    }

    private fun text(raw: String?): String = raw?.trim() ?: ""

    private fun integer(column: Column, raw: String?): Int {
        val value = text(raw)
        if (value.isEmpty()) return 0
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Поле «${column.label}» должно быть числом", e)
        }
    }

    private fun long(column: Column, raw: String?): Long {
        val value = text(raw)
        if (value.isEmpty()) return 0L
        return try {
            value.toLong()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Поле «${column.label}» должно быть числом", e)
        }
    }

    private fun real(column: Column, raw: String?): Double {
        val value = text(raw).replace(',', '.')
        if (value.isEmpty()) return 0.0
        return try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Поле «${column.label}» должно быть числом", e)
        }
    }
}
