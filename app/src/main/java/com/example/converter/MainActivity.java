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
import java.net.URL;
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

    final String LOG_TAG = "myLogs";


    static class InternetCheck extends AsyncTask<Void, Void, Boolean> {

        private Consumer mConsumer;

        public interface Consumer {
            void accept(Boolean internet);
        }

        public InternetCheck(Consumer consumer) {
            mConsumer = consumer;
            execute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);
                sock.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean internet) {
            mConsumer.accept(internet);
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

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionsPagerAdapter);

        tabs.setupWithViewPager(viewPager);

        new InternetCheck(internet -> {
            QueryURL();
        });

        //слушатель свайпа вниз для обновления данных
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new InternetCheck(internet -> {
                            clearTable();
                            QueryURL();
                        });
                        // Отменяем анимацию обновления
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            // закрываем подключение к БД
            dbHelper.close();
            // для перезагрузки фрагментов после получения новых данных
            sectionsPagerAdapter.notifyDataSetChanged();
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
                    new CBR_query().execute(url);//Создаем новый Thread (нить/поток)
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
        //обнуляем значение autoincrement (необязательно)
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'valute'");
    }
}