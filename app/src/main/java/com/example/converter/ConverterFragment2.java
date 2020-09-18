
//Класс второго фрагмента для реализации конвертера валют
package com.example.converter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class ConverterFragment2 extends Fragment {
    private Spinner spinner;
    private Spinner spinner2;
    private ArrayList<String> arrayList_CharCode;
    private ArrayList<String> value_list;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private EditText et_value;
    private TextView text_result;
    private TextView text_info;
    private Button button;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ConverterFragment2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment2.
     */
    // TODO: Rename and change types and number of parameters
    public static ConverterFragment2 newInstance(String param1, String param2) {
        ConverterFragment2 fragment = new ConverterFragment2();
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
        View view = inflater.inflate(R.layout.fragment_blank2, container, false);

        spinner = view.findViewById(R.id.spinner); //       Первый спиннер со списком сокращенных названий валют (можно убрать все валюты кроме рубля и изменить спиннер на editView).
        spinner2 = view.findViewById(R.id.spinner2);//      Второй спиннер со списком сокращенных названий валют
        et_value = view.findViewById(R.id.et_value);//      EditText для ввода суммы конвертируемой валюты
        text_result = view.findViewById(R.id.tv_result);//  Текстовое поле для вывода результата конвертации
        text_info = view.findViewById(R.id.text_info);//    Поле для вывода информации о валютах
        button = view.findViewById(R.id.button);//          Кнопка для свапа валют местами

        //  список сокращенных названий валют для спиннеров (береться из таблицы sqlLite)
        arrayList_CharCode = new ArrayList<>();
        //  список курса валют относительно рубля (береться из таблицы sqlLite)
        value_list = new ArrayList<>();

        UpdateFrame();

        //  слушатель кнопки. меняет валюты местами
        button.setOnClickListener(view1 -> Swap());

        // обработчики (слушатели) нажатий на спиннеры и элементы спиннеров
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                Convert();
            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                Convert();
            }
        });
        //слушатель изменения поля editText1
        et_value.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Convert();
            }
        });

        return view;
    }


    //  функция для выгрузки данных из таблицы и подставления в элементы на экране (спиннеры и текстовые поля)
    public void UpdateFrame() {
        db = connectDB();
        // делаем запрос на выборку всех данных из таблицы valute, получаем Cursor
        Cursor c = db.query("valute", null, null, null, null, null, null);
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        if (c.moveToFirst()) {
            // определяем номера столбцов по имени в выборке
            int charcodeColIndex = c.getColumnIndex("charcode");
            int valueColIndex = c.getColumnIndex("value");
            do {
                // получаем значения по номерам столбцов
                arrayList_CharCode.add(c.getString(charcodeColIndex));
                value_list.add(c.getString(valueColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
            } while (c.moveToNext());
        }
        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, arrayList_CharCode);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner2.setAdapter(adapter);
        spinner2.setSelection(11);//    по умолчанию устанавливаем 11 (USD) элемент списка валют для конвертации с рублем. (не обязательно)
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

    //  конвертация валют
    public void Convert() {
        double a = 1, b = 1;
        double sum = 0, kurs = 1;
        for (int i = 0; i < arrayList_CharCode.size(); i++) {
            if (arrayList_CharCode.get(i).equals(spinner.getSelectedItem())) {
                a = Double.parseDouble(value_list.get(i));  //  из String значения курса сделали Double
            }
            if (arrayList_CharCode.get(i).equals(spinner2.getSelectedItem())) {
                b = Double.parseDouble(value_list.get(i));  //  из String значения курса сделали Double
            }
        }
        kurs = a / b;   //  высчитали курс

        BigDecimal bd = new BigDecimal(Double.toString(kurs));
        bd = bd.setScale(4, RoundingMode.HALF_UP);  //округлили до 4-х знаков после запятой
        text_info.setText("Текущий курс\n1 " + spinner.getSelectedItem() + " = " + bd + " " + spinner2.getSelectedItem());  // вывели информацию о курсах конвертируемых валют
        if (!et_value.getText().toString().equals("")) {
            sum = Double.parseDouble(String.valueOf(et_value.getText())) * bd.doubleValue();   //  получили значение из EditText и сделали его Double
        }
        BigDecimal sum2 = new BigDecimal(Double.toString(sum));
        sum2 = sum2.setScale(2, RoundingMode.HALF_UP);  //  округлили до 2-х знаков после запятой
        text_result.setText("" + sum2); //  подставили в поле результата
    }

    //  функция обмена валют местами (названия валют в спиннерах и значений в текстовых полях)
    public void Swap() {
        int spinner_save;
        String text_save;
        spinner_save = spinner.getSelectedItemPosition();
        spinner.setSelection(spinner2.getSelectedItemPosition());
        spinner2.setSelection(spinner_save);

        text_save = String.valueOf(et_value.getText());
        et_value.setText(text_result.getText());
        text_result.setText(text_save);
    }
}