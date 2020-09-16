package com.example.converter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    final String LOG_TAG = "myLogs";

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "DB_Valute", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table valute ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "charcode text,"
                + "value text,"
                + "previous text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
