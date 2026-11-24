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
                                    db.beginTransaction();
                                    try {
                                        seed(db);
                                        db.setTransactionSuccessful();
                                    } finally {
                                        db.endTransaction();
                                    }
                                }
                            })
                            .build();
                }
            }
        }
        return instance;
    }

    /** Начальное заполнение: 4 дома, 12 собственников, 10 квартир, 12 начислений и т.д. */
    private static void seed(SupportSQLiteDatabase db) {
        db.execSQL("INSERT INTO Houses (Id, Address, YearBuilt, Floors, Entrances) VALUES " +
                "(1, 'ул. Ленина, 10', 2005, 5, 2), " +
                "(2, 'ул. Мира, 25', 2012, 9, 3), " +
                "(3, 'пр. Победы, 7', 1998, 4, 1), " +
                "(4, 'ул. Садовая, 14', 2018, 12, 4)");

        db.execSQL("INSERT INTO Cobstveniki " +
                "(Id, LastName, FirstName, MiddleName, Phone, PassportSeries, PassportNumber) VALUES " +
                "(1, 'Иванов', 'Иван', 'Иванович', '+7 900 123-45-67', '4501', '123456'), " +
                "(2, 'Петрова', 'Мария', 'Сергеевна', '+7 900 234-56-78', '4502', '234567'), " +
                "(3, 'Сидоров', 'Алексей', 'Павлович', '+7 900 345-67-89', '4503', '345678'), " +
                "(4, 'Кузнецова', 'Ольга', 'Викторовна', '+7 900 456-78-90', '4504', '456789'), " +
                "(5, 'Смирнов', 'Дмитрий', 'Андреевич', '+7 900 567-89-01', '4505', '567890'), " +
                "(6, 'Попова', 'Елена', 'Николаевна', '+7 900 678-90-12', '4506', '678901'), " +
                "(7, 'Васильев', 'Сергей', 'Игоревич', '+7 900 789-01-23', '4507', '789012'), " +
                "(8, 'Новикова', 'Анна', 'Дмитриевна', '+7 900 890-12-34', '4508', '890123'), " +
                "(9, 'Морозов', 'Павел', 'Викторович', '+7 900 901-23-45', '4509', '901234'), " +
                "(10, 'Фёдорова', 'Татьяна', 'Олеговна', '+7 900 012-34-56', '4510', '012345'), " +
                "(11, 'Волков', 'Артём', 'Романович', '+7 900 111-22-33', '4511', '112233'), " +
                "(12, 'Лебедева', 'Марина', 'Андреевна', '+7 900 222-33-44', '4512', '223344')");

        db.execSQL("INSERT INTO Yslygi (Id, Name, Tariff) VALUES " +
                "(1, 'Содержание и ремонт', 35.5), " +
                "(2, 'Вывоз ТКО', 7.2), " +
                "(3, 'Отопление', 52.0), " +
                "(4, 'Электроэнергия (общедомовые нужды)', 4.8), " +
                "(5, 'Водоснабжение (общедомовые нужды)', 12.4)");

        db.execSQL("INSERT INTO Kvartira " +
                "(Id, HouseId, ApartmentNumber, EntranceNumber, Area, OwnerId) VALUES " +
                "(1, 1, 12, 1, 54.2, 1), " +
                "(2, 1, 25, 2, 67.8, 2), " +
                "(3, 2, 8, 1, 45.0, 3), " +
                "(4, 3, 41, 1, 78.5, 4), " +
                "(5, 2, 16, 3, 52.0, 5), " +
                "(6, 1, 33, 2, 61.4, 6), " +
                "(7, 4, 5, 1, 48.7, 7), " +
                "(8, 4, 18, 2, 72.3, 8), " +
                "(9, 3, 56, 1, 55.1, 9), " +
                "(10, 2, 29, 2, 63.6, 10)");

        db.execSQL("INSERT INTO Nachislenie " +
                "(Id, ApartmentId, ServiceId, Period, Amount, ChargeDate, PaymentStatus) VALUES " +
                "(1, 1, 1, '2026-11', 1920.00, '2026-11-01', 'Оплачено'), " +
                "(2, 1, 2, '2026-11', 390.00, '2026-11-01', 'Не оплачено'), " +
                "(3, 2, 1, '2026-11', 2404.00, '2026-11-01', 'Оплачено'), " +
                "(4, 3, 3, '2026-10', 2340.00, '2026-10-02', 'Не оплачено'), " +
                "(5, 4, 4, '2026-11', 376.80, '2026-11-01', 'Оплачено'), " +
                "(6, 5, 1, '2026-11', 1844.00, '2026-11-01', 'Оплачено'), " +
                "(7, 6, 2, '2026-11', 442.08, '2026-11-01', 'Не оплачено'), " +
                "(8, 7, 3, '2026-10', 2532.40, '2026-10-02', 'Оплачено'), " +
                "(9, 8, 5, '2026-11', 897.72, '2026-11-01', 'Не оплачено'), " +
                "(10, 9, 1, '2026-11', 1956.05, '2026-11-01', 'Оплачено'), " +
                "(11, 10, 4, '2026-11', 305.28, '2026-11-01', 'Не оплачено'), " +
                "(12, 2, 5, '2026-11', 840.72, '2026-11-01', 'Оплачено')");

        db.execSQL("INSERT INTO Zaivki (Id, OwnerId, RequestDate, Description, Status) VALUES " +
                "(1, 1, '2026-11-03', 'Не работает лифт', 'Выполнено'), " +
                "(2, 3, '2026-11-10', 'Протечка в ванной', 'В работе'), " +
                "(3, 5, '2026-11-15', 'Не горит лампа в подъезде', 'Новая'), " +
                "(4, 7, '2026-11-16', 'Не закрывается входная дверь', 'В работе'), " +
                "(5, 9, '2026-11-18', 'Шум в трубах отопления', 'Новая'), " +
                "(6, 11, '2026-11-20', 'Требуется замена лампы на лестнице', 'Выполнено')");

        db.execSQL("INSERT INTO Rashod (Id, ExpenseItem, Amount, Date, HouseId) VALUES " +
                "(1, 'Электричество подъездов', 1500.00, '2026-11-05', 1), " +
                "(2, 'Вывоз мусора', 2300.00, '2026-11-06', 1), " +
                "(3, 'Ремонт крыши', 18000.00, '2026-10-20', 2), " +
                "(4, 'Уборка территорий', 4200.00, '2026-11-08', 3), " +
                "(5, 'Замена освещения', 3150.00, '2026-11-12', 4), " +
                "(6, 'Обслуживание лифтов', 8700.00, '2026-11-14', 2)");

        // Триггеры целостности: недопустимые значения статусов и отрицательные суммы.
        db.execSQL("CREATE TRIGGER IF NOT EXISTS trg_zaivki_status_insert " +
                "BEFORE INSERT ON Zaivki WHEN NEW.Status NOT IN ('Новая', 'В работе', 'Выполнено') " +
                "BEGIN SELECT RAISE(ABORT, 'Недопустимый статус заявки'); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS trg_zaivki_status_update " +
                "BEFORE UPDATE OF Status ON Zaivki " +
                "WHEN NEW.Status NOT IN ('Новая', 'В работе', 'Выполнено') " +
                "BEGIN SELECT RAISE(ABORT, 'Недопустимый статус заявки'); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS trg_nachislenie_status " +
                "BEFORE UPDATE OF PaymentStatus ON Nachislenie " +
                "WHEN NEW.PaymentStatus NOT IN ('Оплачено', 'Не оплачено') " +
                "BEGIN SELECT RAISE(ABORT, 'Недопустимый статус оплаты'); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS trg_nachislenie_amount " +
                "BEFORE INSERT ON Nachislenie WHEN NEW.Amount < 0 " +
                "BEGIN SELECT RAISE(ABORT, 'Сумма начисления не может быть отрицательной'); END");
        db.execSQL("CREATE TRIGGER IF NOT EXISTS trg_rashod_amount " +
                "BEFORE INSERT ON Rashod WHEN NEW.Amount < 0 " +
                "BEGIN SELECT RAISE(ABORT, 'Сумма расхода не может быть отрицательной'); END");
    }
}
