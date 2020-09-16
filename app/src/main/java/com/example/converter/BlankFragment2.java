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

public class BlankFragment2 extends Fragment {
    private Spinner spinner;
    private Spinner spinner2;
    private ArrayList<String> arrayList_CharCode;
    private ArrayList<String> value_list;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private EditText editText1;
    private TextView editText2;
    private TextView textView;
    private Button button;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BlankFragment2() {
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
    public static BlankFragment2 newInstance(String param1, String param2) {
        BlankFragment2 fragment = new BlankFragment2();
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

        spinner = view.findViewById(R.id.spinner);
        spinner2 = view.findViewById(R.id.spinner2);
        editText1 = view.findViewById(R.id.et_value);
        editText2 = view.findViewById(R.id.et_value2);
        textView = view.findViewById(R.id.textView);
        button = view.findViewById(R.id.button);

        arrayList_CharCode = new ArrayList<>();
        value_list = new ArrayList<>();

        UpdateFrame();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Swap();
            }
        });

        // устанавливаем обработчик нажатия
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
        // устанавливаем обработчик нажатия
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
        editText1.addTextChangedListener(new TextWatcher() {

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

    public void Convert() {
        double a = 1, b = 1;
        double sum = 0, kurs = 1;
        for (int i = 0; i < arrayList_CharCode.size(); i++) {
            if (arrayList_CharCode.get(i).equals(spinner.getSelectedItem())) {
                a = Double.parseDouble(value_list.get(i));
            }
            if (arrayList_CharCode.get(i).equals(spinner2.getSelectedItem())) {
                b = Double.parseDouble(value_list.get(i));
            }
        }
        kurs = a / b;


        BigDecimal bd = new BigDecimal(Double.toString(kurs));
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        textView.setText("Текущий курс\n1 " + spinner.getSelectedItem() + " = " + bd + " " + spinner2.getSelectedItem());
        if (!editText1.getText().toString().equals("")) {
            sum = Double.parseDouble(String.valueOf(editText1.getText())) * bd.doubleValue();
        }
        BigDecimal sum2 = new BigDecimal(Double.toString(sum));
        sum2 = sum2.setScale(2, RoundingMode.HALF_UP);
        editText2.setText("" + sum2);
    }

    public void Swap() {
        int spinner_save;
        spinner_save = spinner.getSelectedItemPosition();
        spinner.setSelection(spinner2.getSelectedItemPosition());
        spinner2.setSelection(spinner_save);
    }
}