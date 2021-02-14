package com.example.androidtrlts.Preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SeekBarPreference;

public class MySeekbarPreference extends SeekBarPreference {
    public MySeekbarPreference(Context context){
        super(context);
    }

    public MySeekbarPreference(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public MySeekbarPreference(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);

    }
}
