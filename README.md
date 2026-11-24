# ManagementCompany (Android)

Название приложения в лаунчере и интерфейсе — **«Управляющая компания»**.
Папка проекта — `ManagementCompany`, пакет — `com.example.managementcompany`,
файл базы данных — `uk.sqlite`.

Работа с данными построена на **Android Room** (не на прямом `SQLiteOpenHelper`):
сущности `@Entity`, `AppDao`, `AppDatabase` и слой `AppRepository`, который подключают экраны.

## What's inside

- 7 разделов: дома, квартиры, собственники, услуги, начисления, заявки, расходы.
- Полный CRUD в отдельных экранах добавления/изменения.
- Панель команд: навигация по записям, поиск по `Id`, открытие, изменение, удаление,
  карточка собственника.
- Главный экран — рабочий стол: сводка, финансы с индикатором оплаты, последние заявки,
  быстрые действия и реестры.
- Просмотр любой записи: структурированные карточки «подпись — значение», расшифровка внешних
  ключей и переходы в связанные записи. У заявок и начислений — смена статуса.
- Карточка собственника с вкладками «Собственник» и «Записи».
- Однотонные иконки, кнопки и шапки; системный `ActionBar` отключён.

## Room

| Файл | Роль |
| --- | --- |
| `HouseEntity.java`, `ApartmentEntity.java`, `OwnerEntity.java`, `ServiceEntity.java`, `ChargeEntity.java`, `RequestEntity.java`, `ExpenseEntity.java` | `@Entity` с теми же именами таблиц и столбцов |
| `AppDao.java` | `@Insert`, `@Update`, `@Delete`, `@RawQuery`, `@Query` |
| `AppDatabase.java` | `@Database`, синглтон, стартовые данные в `Callback.onCreate()` |
| `AppRepository.java` | Общий API для экранов: выборки, фильтры, справочники, агрегаты |
| `TableDefinition.java` | Описание таблиц и подписей полей |

Внешние ключи объявлены через `@ForeignKey`, для полей-ссылок включены индексы.
Room создаёт базу `uk.sqlite` при первом запуске и заполняет её демонстрационными данными.
Для синхронных вызовов из экранов включён `allowMainThreadQueries()`.

## Сборка

Откройте папку `ManagementCompany` в Android Studio и нажмите **Run**, либо:

```bat
gradlew.bat assembleDebug
```

APK: `app\build\outputs\apk\debug\app-debug.apk`.

Стек: Java 17, Android SDK (minSdk 24, compileSdk/targetSdk 37), Room 2.8.5,
AGP 9.4.0 / Gradle 9.6.0, без других сторонних библиотек.
