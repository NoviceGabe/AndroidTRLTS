package com.example.androidtrlts.Adapters;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;

import com.example.androidtrlts.Activities.MainActivity;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.Util;

import java.util.List;

public class SortAdapter extends CustomBaseAdapter{
    private Context context;
    private RadioButton radioButton;
    public static int selectedPosition = 0;

    public SortAdapter(Context context, List<String> items) {
        super(context);
        this.items = items;
        this.context = context;
        layoutId = R.layout.sort_item;
    }

    @Override
    public View getView(View view, int pos) {
        String item = (String) items.get(pos);
        radioButton = view.findViewById(R.id.radio_btn);
        radioButton.setText(item);
        radioButton.setChecked(pos == selectedPosition);
        radioButton.setTag(pos);

        radioButton.setOnClickListener(v -> {
            selectedPosition = (Integer)v.getTag();
            if(selectedPosition == 1){
                MainActivity.property = Util.Property.DATE;
            }else{
                MainActivity.property = Util.Property.NAME;
            }
            notifyDataSetChanged();
        });

        return view;
    }
}
