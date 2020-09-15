package com.example.converter;

import android.os.AsyncTask;
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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import static com.example.converter.DAO.getResponceFromURL;


public class BlankFragment2 extends Fragment {
    private ArrayList<Valute> arrayList;
    private Spinner spinner;
    private Spinner spinner2;
    private ArrayList<String> arrayList_CharCode;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank2, container, false);
        spinner = view.findViewById(R.id.spinner);
        spinner2 = view.findViewById(R.id.spinner2);
        editText1 = view.findViewById(R.id.et_value);
        editText2 = view.findViewById(R.id.et_value2);
        textView = view.findViewById(R.id.textView);
        arrayList_CharCode = new ArrayList<>();
        QueryURL();
        button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Swap();
            }
        });

        return view;
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
            arrayList = new ArrayList<>();
            String Name = null;
            String CharCode = null;
            String Value = null;
            String Previous = null;
            Valute valute = null;
            try {
                JSONObject jsonResponce = new JSONObject(responce);
                JSONObject jsonObject = jsonResponce.getJSONObject("Valute");
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Name = jsonObject.getJSONObject(key).getString("Name");
                    CharCode = jsonObject.getJSONObject(key).getString("CharCode");
                    Value = jsonObject.getJSONObject(key).getString("Value");
                    Previous = jsonObject.getJSONObject(key).getString("Previous");
                    valute = new Valute(Name, CharCode, Value, Previous);
                    arrayList.add(valute);
                }
                for (int i = 0; i < arrayList.size(); i++) {
                    arrayList_CharCode.add(arrayList.get(i).getCharCode());
                }
                // адаптер
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, arrayList_CharCode);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner2.setAdapter(adapter);
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
                editText1.addTextChangedListener(new TextWatcher(){

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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void QueryURL() {
        URL url = null;
        try {
            url = new URL("https://www.cbr-xml-daily.ru/daily_json.js");
            new CBR_query().execute(url);
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void Convert(){
        double a = 0, b = 0;
        double sum = 0, kurs = 0;
        for(int i = 0; i < arrayList.size(); i++){
            if(arrayList.get(i).getCharCode().equals(spinner.getSelectedItem())){
                a = Double.parseDouble(arrayList.get(i).getValue());
            }
            if(arrayList.get(i).getCharCode().equals(spinner2.getSelectedItem())){
                b = Double.parseDouble(arrayList.get(i).getValue());
            }
        }
        kurs = a / b;

        BigDecimal bd = new BigDecimal(Double.toString(kurs));
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        textView.setText("Текущий курс\n1 " + spinner.getSelectedItem() + " = " + bd + " " + spinner2.getSelectedItem());
        sum = Double.parseDouble(String.valueOf(editText1.getText()))*bd.doubleValue();
        BigDecimal sum2 = new BigDecimal(Double.toString(sum));
        sum2 = sum2.setScale(2, RoundingMode.HALF_UP);
        editText2.setText("" + sum2);
    }

    public void Swap(){
        int spinner_save;
        spinner_save = spinner.getSelectedItemPosition();
        spinner.setSelection(spinner2.getSelectedItemPosition());
        spinner2.setSelection(spinner_save);
    }
}