
//  класс адаптер для отображения списка валют
package com.example.converter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private ArrayList<Valute> arrayList;

    class ListViewHolder extends RecyclerView.ViewHolder {
        TextView nameItemView;
        TextView CharCodeItemView;
        TextView ValueItemView;
        TextView PreviousItemView;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            nameItemView = itemView.findViewById(R.id.tv_name_item);
            CharCodeItemView = itemView.findViewById(R.id.tv_CharCode_item);
            ValueItemView = itemView.findViewById(R.id.tv_Value_item);
            PreviousItemView = itemView.findViewById(R.id.tv_Previous_item);
        }

        void bind(Valute valute) {
            nameItemView.setText(valute.getName());
            CharCodeItemView.setText(valute.getCharCode());
            ValueItemView.setText("1 " + valute.getCharCode() + " = " + valute.getValue() + " RUB");
            PreviousItemView.setText("1 " + valute.getCharCode() + " = " + valute.getPrevious() + " RUB");
        }
    }


    public void setItems(ArrayList<Valute> valutesList) {
        arrayList = valutesList;
    }

    public void clearItems() {
        arrayList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.list_item;
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, parent, false);
        ListViewHolder listViewHolder = new ListViewHolder(view);
        return listViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.bind(arrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public Valute getItem(int i){
        return arrayList.get(i);
    }
}
