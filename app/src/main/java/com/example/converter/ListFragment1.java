
//Класс первого фрагмента для вывода списка валют на экран с помощью RecyclerList
package com.example.converter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment1 extends Fragment {

    //  объявление переменных
    private ArrayList<Valute> arrayList;
    private RecyclerView recyclerList;
    private ListAdapter listAdapter;
    private Valute valute;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ListFragment1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment1.
     */
    // TODO: Rename and change types and number of parameters
    public static ListFragment1 newInstance(String param1, String param2) {
        ListFragment1 fragment = new ListFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank1, container, false);

        recyclerList = view.findViewById(R.id.recyclerView);    //  список для вывода данных

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerList.setLayoutManager(layoutManager);
        recyclerList.setHasFixedSize(true);

        UpdateFrame();

        return view;
    }
    //  функция для выгрузки данных из таблицы и передачи адаптеру списка объектов
    public void UpdateFrame() {
        listAdapter = new ListAdapter();
        arrayList = new ArrayList<>();
        db = connectDB();
        // делаем запрос на выборку всех данных из таблицы valute, получаем Cursor
        Cursor c = db.query("valute", null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int nameColIndex = c.getColumnIndex("name");
            int charcodeColIndex = c.getColumnIndex("charcode");
            int valueColIndex = c.getColumnIndex("value");
            int previousColIndex = c.getColumnIndex("previous");
            c.moveToNext();
            do {
                // получаем значения по номерам столбцов
                //  создаем объект javabean и передаем туда наши данные
                valute = new Valute(c.getString(nameColIndex), c.getString(charcodeColIndex), c.getString(valueColIndex), c.getString(previousColIndex));
                //  добавляем объект в список
                arrayList.add(valute);
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        //  передаем адаптеру полученный список объектов класса Valute
        listAdapter.setItems(arrayList);
        //  устанавливаем адаптер для recycler list
        recyclerList.setAdapter(listAdapter);
        c.close();
    }
    //  для подключения к БД
    public SQLiteDatabase connectDB() {
        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(getContext());
        // подключаемся к БД
        db = dbHelper.getWritableDatabase();
        return db;
    }
}