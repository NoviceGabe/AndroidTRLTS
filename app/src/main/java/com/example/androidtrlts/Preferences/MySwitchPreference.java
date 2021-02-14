package com.example.androidtrlts.Preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

public class MySwitchPreference extends SwitchPreference {

    public MySwitchPreference(Context context){
        super(context);
    }

    public MySwitchPreference(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public MySwitchPreference(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
    }
}
