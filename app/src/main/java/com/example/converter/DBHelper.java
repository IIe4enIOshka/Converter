
//  Класс управления БД
package com.example.converter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    final String LOG_TAG = "myLogs";

    // Fields for first table
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database.db";

    private static final String TABLE_VALUTE = "valute";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CHARCODE = "charcode";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_PREVIOUS = "previous";

    // Fields for second table
    private static final String TABLE_DATE = "date_query";
    private static final String COLUMN_DATE_TEXT = "date_text";

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String DATABASE_CREATE_TABLE1 = "create table " //  таблица для хранения списка валют
            + TABLE_VALUTE
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text not null, "
            + COLUMN_CHARCODE + " text not null, "
            + COLUMN_VALUE + " text not null, "
            + COLUMN_PREVIOUS + " text not null"
            + ");";

    public static final String DATABASE_CREATE_TABLE2 = "create table " //  таблица для хранения даты и времени последнего обновления данных
            + TABLE_DATE
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DATE_TEXT + " text not null"
            + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_TABLE1);
        db.execSQL(DATABASE_CREATE_TABLE2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
