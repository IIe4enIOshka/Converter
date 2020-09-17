package com.example.converter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import static com.example.converter.DAO.getResponceFromURL;


public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private ContentValues cv;
    private ViewPager viewPager;
    private TabLayout tabs;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SectionsPagerAdapter sectionsPagerAdapter;

    private TextView text_data;
    private TextView no_internet;

    final String LOG_TAG = "myLogs";

    class InternetCheck extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                int exitValue = ipProcess.waitFor();
                System.out.println(exitValue);
                return (exitValue == 1);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean internet) {
            if (internet) {
                clearTable();
                QueryURL();
            } else {
                no_internet.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        no_internet.setVisibility(View.GONE);
                    }
                }, 5000);

            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //получаем объекты
        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        text_data = findViewById(R.id.data);
        no_internet = findViewById(R.id.no_internet);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ArrayList<String> array = new ArrayList<>();

        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

        UpdateDate();
        QueryURL();

        //слушатель свайпа вниз для обновления данных
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new InternetCheck().execute();
                new Runnable() {
                    @Override
                    public void run() {
                    }
                };
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(
                Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
    }

    class CBR_query extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response = null;
            try {
                response = getResponceFromURL(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String responce) {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yy");
            Date date = new Date();
            // создаем объект для данных
            cv = new ContentValues();
            // подключаемся к БД
            db = connectDB();
            String Name = "";
            String CharCode = "";
            String Value = "";
            String Previous = "";
            try {
                JSONObject jsonResponce = new JSONObject(responce);
                JSONObject jsonObject = jsonResponce.getJSONObject("Valute");
                Iterator<String> keys = jsonObject.keys();

                // подключаемся к БД
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                cv.put("name", "Российский рубль");
                cv.put("charcode", "RUB");
                cv.put("value", "1");
                cv.put("previous", "1");
                // вставляем запись
                db.insert("valute", null, cv);
                while (keys.hasNext()) {
                    String key = keys.next();
                    Name = jsonObject.getJSONObject(key).getString("Name");
                    CharCode = jsonObject.getJSONObject(key).getString("CharCode");
                    Value = jsonObject.getJSONObject(key).getString("Value");
                    Previous = jsonObject.getJSONObject(key).getString("Previous");

                    cv.put("name", Name);
                    cv.put("charcode", CharCode);
                    cv.put("value", Value);
                    cv.put("previous", Previous);
                    // вставляем запись
                    db.insert("valute", null, cv);
                }
                // создаем объект для данных
                ContentValues cv1 = new ContentValues();
                cv1.put("date_text", "" + dateFormat.format(date));
                db.insert("date_query", null, cv1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // закрываем подключение к БД
            dbHelper.close();
            // для перезагрузки фрагментов после получения новых данных
            sectionsPagerAdapter.notifyDataSetChanged();
            text_data.setText(dateFormat.format(date));
        }
    }

    public void UpdateDate(){
        // подключаемся к БД
        db = connectDB();
        Cursor cur1 = db.rawQuery("SELECT * FROM date_query", null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (cur1.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int dateColIndex = cur1.getColumnIndex("date_text");
            do {
                text_data.setText(cur1.getString(dateColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (cur1.moveToNext());
        }
    }

    //Для отправки запроса на сайт для получения новых данных о валютах
    public void QueryURL() {
        Log.d(LOG_TAG, "запрос");
        URL url = null;
        // подключаемся к БД
        db = connectDB();
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM valute", null);
        if (cur != null) {
            cur.moveToFirst();
            if (cur.getInt(0) == 0) {
                try {
                    url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");
                    new CBR_query().execute(url);// Создаем новый Thread (нить/поток)
                } catch (
                        IOException e) {
                    e.printStackTrace();
                }
            }
        }
        cur.close();
        dbHelper.close();
    }

    //Для подключения к БД
    public SQLiteDatabase connectDB() {
        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);
        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        return db;
    }

    //Для очистки таблицы
    public void clearTable() {
        // подключаемся к БД
        db = connectDB();
        // удаляем все записи
        db.delete("valute", null, null);
        db.delete("date_query", null, null);
        //обнуляем значение autoincrement (необязательно)
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'valute'");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'date_query'");
    }
}