package com.example.androidtrlts.Preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;

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
        SessionHelper sessionHelper = new SessionHelper(getContext());

        TextView title = (TextView) holder.findViewById(android.R.id.title);
        title.setTextColor(getContext().getResources().getColor(R.color.textColor));
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
        summary.setTextColor(getContext().getResources().getColor(R.color.textColorSecondary));
    }
}
