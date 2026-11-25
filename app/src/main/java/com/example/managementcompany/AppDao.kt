package com.example.managementcompany

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery

/** DAO для всех таблиц приложения. */
@Dao
interface AppDao {

    @RawQuery
    fun selectHouses(query: SimpleSQLiteQuery): List<HouseEntity>

    @RawQuery
    fun selectApartments(query: SimpleSQLiteQuery): List<ApartmentEntity>

    @RawQuery
    fun selectOwners(query: SimpleSQLiteQuery): List<OwnerEntity>

    @RawQuery
    fun selectServices(query: SimpleSQLiteQuery): List<ServiceEntity>

    @RawQuery
    fun selectCharges(query: SimpleSQLiteQuery): List<ChargeEntity>

    @RawQuery
    fun selectRequests(query: SimpleSQLiteQuery): List<RequestEntity>

    @RawQuery
    fun selectExpenses(query: SimpleSQLiteQuery): List<ExpenseEntity>

    @RawQuery
    fun selectRefRows(query: SimpleSQLiteQuery): List<RefRow>

    @RawQuery
    fun selectInt(query: SimpleSQLiteQuery): Int

    @RawQuery
    fun selectDouble(query: SimpleSQLiteQuery): Double

    @Insert
    fun insertHouse(entity: HouseEntity): Long

    @Insert
    fun insertApartment(entity: ApartmentEntity): Long

    @Insert
    fun insertOwner(entity: OwnerEntity): Long

    @Insert
    fun insertService(entity: ServiceEntity): Long

    @Insert
    fun insertCharge(entity: ChargeEntity): Long

    @Insert
    fun insertRequest(entity: RequestEntity): Long

    @Insert
    fun insertExpense(entity: ExpenseEntity): Long

    @Update
    fun updateHouse(entity: HouseEntity): Int

    @Update
    fun updateApartment(entity: ApartmentEntity): Int

    @Update
    fun updateOwner(entity: OwnerEntity): Int

    @Update
    fun updateService(entity: ServiceEntity): Int

    @Update
    fun updateCharge(entity: ChargeEntity): Int

    @Update
    fun updateRequest(entity: RequestEntity): Int

    @Update
    fun updateExpense(entity: ExpenseEntity): Int

    @Delete
    fun deleteHouse(entity: HouseEntity)

    @Delete
    fun deleteApartment(entity: ApartmentEntity)

    @Delete
    fun deleteOwner(entity: OwnerEntity)

    @Delete
    fun deleteService(entity: ServiceEntity)

    @Delete
    fun deleteCharge(entity: ChargeEntity)

    @Delete
    fun deleteRequest(entity: RequestEntity)

    @Delete
    fun deleteExpense(entity: ExpenseEntity)

    @Query("SELECT COUNT(*) FROM Zaivki WHERE Status IN ('Новая', 'В работе')")
    fun countActiveRequests(): Int

    /** Атомарная смена статуса заявки (аппарат транзакций Room). */
    @Transaction
    @Query("UPDATE Zaivki SET Status = :status WHERE Id = :id")
    fun setRequestStatus(id: Long, status: String): Int

    /** Атомарная смена статуса оплаты (аппарат транзакций Room). */
    @Transaction
    @Query("UPDATE Nachislenie SET PaymentStatus = :status WHERE Id = :id")
    fun setChargeStatus(id: Long, status: String): Int
}
