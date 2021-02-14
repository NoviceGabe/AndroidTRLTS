package com.example.androidtrlts.Helpers;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SessionHelper {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String PREF = "session";
    private Context context;

    public SessionHelper(Context context){
        this.context = context;
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void initDefaultSharedPreferences(){
        prefs =  PreferenceManager.getDefaultSharedPreferences(this.context);
    }
    public void setSession(String key, String name){
        editor = prefs.edit();
        editor.putString(key, name).apply();
    }

    public void setSession(String key, int name){
        editor = prefs.edit();
        editor.putInt(key, name).apply();
    }

    public void setSession(String key, float name){
        editor = prefs.edit();
        editor.putFloat(key, name).apply();
    }

    public void setSession(String key, boolean name){
        editor = prefs.edit();
        editor.putBoolean(key, name).apply();
    }

    public void removeSession(String key){
        editor = prefs.edit();
        editor.remove(key).apply();
    }

    public String getSessionString(String key, String defaultValue){
        return prefs.getString(key, defaultValue);
    }

    public int getSessionInt(String key,  int defaultValue){
        return prefs.getInt(key, defaultValue);
    }

    public String getSessionString(String key){
        return prefs.getString(key, "");
    }

    public int getSessionInt(String key){
        return prefs.getInt(key, 0);
    }

    public float getSessionFloat(String key){
        return prefs.getFloat(key, 0);
    }

    public boolean getSessionBoolean(String key){
        return prefs.getBoolean(key, false);
    }

    public boolean has(String key){
        return prefs.contains(key);
    }

    public boolean clearAllSession(){
        editor = prefs.edit();
        return editor.clear().commit();
    }
}
