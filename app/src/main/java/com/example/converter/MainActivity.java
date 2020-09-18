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
import android.view.View;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private CBR_query query;
    private int time_update = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        text_data = findViewById(R.id.data);    //  поле даты последнего обновления данных
        no_internet = findViewById(R.id.no_internet);   //  информационное поле

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

        starting_checks();
        UpdateDate();

        //  слушатель свайпа вниз для обновления данных
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                InternetCheck();
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

    //  стартовые проверки на наличие данных и их "свежесть"
    public void starting_checks() {
        //  при старте проверяем есть ли у нас уже таблица с данными
        db = connectDB();
        Cursor cur = db.rawQuery("SELECT COUNT(*) FROM valute", null);
        if (cur != null) {
            cur.moveToFirst();
            if (cur.getInt(0) == 0) {
                InternetCheck();
            } else {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yy");
                Date date_new = new Date();
                Date date_old = null;

                Cursor cur1 = db.rawQuery("SELECT * FROM date_query", null);
                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false
                if (cur1.moveToFirst()) {
                    // определяем номера столбцов по имени в выборке
                    int dateColIndex = cur1.getColumnIndex("date_text");
                    do {
                        try {
                            date_old = new SimpleDateFormat("HH:mm dd.MM.yy").parse(cur1.getString(dateColIndex));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        // переход на следующую строку
                        // а если следующей нет (текущая - последняя), то false - выходим из цикла
                    } while (cur1.moveToNext());
                }

                long milliseconds = date_new.getTime() - date_old.getTime();
                int minutes = (int) (milliseconds / (60 * 1000));
//                int hours = (int) (milliseconds / (60 * 60 * 1000));
//                int days = (int) (milliseconds / (24 * 60 * 60 * 1000));
                if (minutes > time_update) {  //  устанавливаем время после которого данные считаются устаревшими и нужно обновление
                    InternetCheck();
                }
            }
        }
        cur.close();
        dbHelper.close();
    }

    //  Проверка наличия интернета с помощью hasConnection
    public void InternetCheck() {
        if (hasConnection(this)) {
            no_internet.setVisibility(View.GONE);
            QueryURL();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            no_internet.setVisibility(View.VISIBLE);
        }
    }

    //  проверка интернет соединения
    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        return false;
    }

    //  для отправки запроса на сайт для получения новых данных о валютах
    public void QueryURL() {
        URL url = null;
        try {
            url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");
            query = new CBR_query();
            query.execute(url);// Создаем новый Thread (нить/поток)
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    //  класс расширенный AsyncTask для отправки ассинхроного запроса к сайту
    class CBR_query extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            String response = null;
            try {
                response = getResponceFromURL(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response != null)
                return response;
            else return null;
        }

        @Override
        protected void onPostExecute(String response) {
            clearTable();
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
                JSONObject jsonResponce = new JSONObject(response);
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
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void UpdateDate() {
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