package com.example.androidtrlts.Preferences;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.rarepebble.colorpicker.ColorPreference;

public class MyColorPreference extends ColorPreference {
    public MyColorPreference(Context context){
        super(context);
    }

    public MyColorPreference(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
    }
}
