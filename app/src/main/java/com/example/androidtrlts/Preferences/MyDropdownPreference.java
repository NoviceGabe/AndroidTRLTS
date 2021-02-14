package com.example.androidtrlts.Preferences;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceViewHolder;

public class MyDropdownPreference extends DropDownPreference {
    private boolean pref_dark_mode;
    private TextView textView;

    public MyDropdownPreference(Context context){
        super(context);
    }

    public MyDropdownPreference(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public MyDropdownPreference(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView title = (TextView) holder.findViewById(android.R.id.title);
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
    }
}
