package com.example.managementcompany;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

/** Локальная база данных на Room. */
@Database(entities = {
        HouseEntity.class,
        ApartmentEntity.class,
        OwnerEntity.class,
        ServiceEntity.class,
        ChargeEntity.class,
        RequestEntity.class,
        ExpenseEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "uk.sqlite";

    private static volatile AppDatabase instance;

    public abstract AppDao dao();

    static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    seed(db);
                                }
                            })
                            .build();
                }
            }
        }
        return instance;
    }

    private static void seed(SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO Houses (Id, Address, YearBuilt, Floors, Entrances) VALUES " +
                "(1, 'ул. Ленина, 10', 2005, 5, 2), " +
                "(2, 'ул. Мира, 25', 2012, 9, 3), " +
                "(3, 'пр. Победы, 7', 1998, 4, 1)");

        db.execSQL("INSERT INTO Cobstveniki " +
                "(Id, LastName, FirstName, MiddleName, Phone, PassportSeries, PassportNumber) VALUES " +
                "(1, 'Иванов', 'Иван', 'Иванович', '+7 900 123-45-67', '4501', '123456'), " +
                "(2, 'Петрова', 'Мария', 'Сергеевна', '+7 900 234-56-78', '4502', '234567'), " +
                "(3, 'Сидоров', 'Алексей', 'Павлович', '+7 900 345-67-89', '4503', '345678'), " +
                "(4, 'Кузнецова', 'Ольга', 'Викторовна', '+7 900 456-78-90', '4504', '456789'), " +
                "(5, 'Смирнов', 'Дмитрий', 'Андреевич', '+7 900 567-89-01', '4505', '567890')");

        db.execSQL("INSERT INTO Yslygi (Id, Name, Tariff) VALUES " +
                "(1, 'Содержание и ремонт', 35.5), " +
                "(2, 'Вывоз ТКО', 7.2), " +
                "(3, 'Отопление', 52.0), " +
                "(4, 'Электроэнергия (общедомовые нужды)', 4.8)");

        db.execSQL("INSERT INTO Kvartira " +
                "(Id, HouseId, ApartmentNumber, EntranceNumber, Area, OwnerId) VALUES " +
                "(1, 1, 12, 1, 54.2, 1), " +
                "(2, 1, 25, 2, 67.8, 2), " +
                "(3, 2, 8, 1, 45.0, 3), " +
                "(4, 3, 41, 1, 78.5, 4), " +
                "(5, 2, 16, 3, 52.0, 5)");

        db.execSQL("INSERT INTO Nachislenie " +
                "(Id, ApartmentId, ServiceId, Period, Amount, ChargeDate, PaymentStatus) VALUES " +
                "(1, 1, 1, '2026-11', 1920.00, '2026-11-01', 'Оплачено'), " +
                "(2, 1, 2, '2026-11', 390.00, '2026-11-01', 'Не оплачено'), " +
                "(3, 2, 1, '2026-11', 2404.00, '2026-11-01', 'Оплачено'), " +
                "(4, 3, 3, '2026-10', 2340.00, '2026-10-02', 'Не оплачено'), " +
                "(5, 4, 4, '2026-11', 376.80, '2026-11-01', 'Оплачено')");

        db.execSQL("INSERT INTO Zaivki (Id, OwnerId, RequestDate, Description, Status) VALUES " +
                "(1, 1, '2026-11-03', 'Не работает лифт', 'Выполнено'), " +
                "(2, 3, '2026-11-10', 'Протечка в ванной', 'В работе'), " +
                "(3, 5, '2026-11-15', 'Не горит лампа в подъезде', 'Новая')");

        db.execSQL("INSERT INTO Rashod (Id, ExpenseItem, Amount, Date, HouseId) VALUES " +
                "(1, 'Электричество подъездов', 1500.00, '2026-11-05', 1), " +
                "(2, 'Вывоз мусора', 2300.00, '2026-11-06', 1), " +
                "(3, 'Ремонт крыши', 18000.00, '2026-10-20', 2), " +
                "(4, 'Уборка территорий', 4200.00, '2026-11-08', 3)");
    }
}
