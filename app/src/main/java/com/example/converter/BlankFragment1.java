package com.example.converter;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import static com.example.converter.DAO.getResponceFromURL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BlankFragment1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment1 extends Fragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Valute> arrayList;
    private RecyclerView recyclerList;
    private ListAdapter listAdapter;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BlankFragment1() {
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
    public static BlankFragment1 newInstance(String param1, String param2) {
        BlankFragment1 fragment = new BlankFragment1();
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
        View view = inflater.inflate(R.layout.fragment_blank1, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerList = view.findViewById(R.id.recyclerView);
        QueryURL();
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(listAdapter != null) {
                            listAdapter.clearItems();
                            QueryURL();
                        }
                        // Отменяем анимацию обновления
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        mSwipeRefreshLayout.setColorSchemeColors(
                Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
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
            } catch (JSONException e) {
                e.printStackTrace();
            }

            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recyclerList.setLayoutManager(layoutManager);
            recyclerList.setHasFixedSize(true);
            listAdapter = new ListAdapter();
            listAdapter.setItems(arrayList);
            recyclerList.setAdapter(listAdapter);
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

    public ListAdapter getList() {
        return listAdapter;
    }
}