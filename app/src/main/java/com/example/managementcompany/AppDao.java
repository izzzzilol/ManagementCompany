package com.example.managementcompany;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.List;

/** DAO для всех таблиц приложения. */
@Dao
public interface AppDao {

    @RawQuery
    List<HouseEntity> selectHouses(SimpleSQLiteQuery query);

    @RawQuery
    List<ApartmentEntity> selectApartments(SimpleSQLiteQuery query);

    @RawQuery
    List<OwnerEntity> selectOwners(SimpleSQLiteQuery query);

    @RawQuery
    List<ServiceEntity> selectServices(SimpleSQLiteQuery query);

    @RawQuery
    List<ChargeEntity> selectCharges(SimpleSQLiteQuery query);

    @RawQuery
    List<RequestEntity> selectRequests(SimpleSQLiteQuery query);

    @RawQuery
    List<ExpenseEntity> selectExpenses(SimpleSQLiteQuery query);

    @RawQuery
    List<RefRow> selectRefRows(SimpleSQLiteQuery query);

    @RawQuery
    int selectInt(SimpleSQLiteQuery query);

    @RawQuery
    double selectDouble(SimpleSQLiteQuery query);

    @Insert
    long insertHouse(HouseEntity entity);

    @Insert
    long insertApartment(ApartmentEntity entity);

    @Insert
    long insertOwner(OwnerEntity entity);

    @Insert
    long insertService(ServiceEntity entity);

    @Insert
    long insertCharge(ChargeEntity entity);

    @Insert
    long insertRequest(RequestEntity entity);

    @Insert
    long insertExpense(ExpenseEntity entity);

    @Update
    int updateHouse(HouseEntity entity);

    @Update
    int updateApartment(ApartmentEntity entity);

    @Update
    int updateOwner(OwnerEntity entity);

    @Update
    int updateService(ServiceEntity entity);

    @Update
    int updateCharge(ChargeEntity entity);

    @Update
    int updateRequest(RequestEntity entity);

    @Update
    int updateExpense(ExpenseEntity entity);

    @Delete
    void deleteHouse(HouseEntity entity);

    @Delete
    void deleteApartment(ApartmentEntity entity);

    @Delete
    void deleteOwner(OwnerEntity entity);

    @Delete
    void deleteService(ServiceEntity entity);

    @Delete
    void deleteCharge(ChargeEntity entity);

    @Delete
    void deleteRequest(RequestEntity entity);

    @Delete
    void deleteExpense(ExpenseEntity entity);

    @Query("SELECT COUNT(*) FROM Zaivki WHERE Status IN ('Новая', 'В работе')")
    int countActiveRequests();

    /** Атомарная смена статуса заявки (аппарат транзакций Room). */
    @Transaction
    @Query("UPDATE Zaivki SET Status = :status WHERE Id = :id")
    int setRequestStatus(long id, String status);

    /** Атомарная смена статуса оплаты (аппарат транзакций Room). */
    @Transaction
    @Query("UPDATE Nachislenie SET PaymentStatus = :status WHERE Id = :id")
    int setChargeStatus(long id, String status);
}
