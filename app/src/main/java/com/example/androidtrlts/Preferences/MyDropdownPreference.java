package com.example.androidtrlts.Preferences;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceViewHolder;

import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;

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
        title.setTextColor(getContext().getResources().getColor(R.color.textColor));
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
        summary.setTextColor(getContext().getResources().getColor(R.color.textColorSecondary));
    }
}
