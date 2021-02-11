package com.example.androidtrlts.Helpers;


import android.content.Context;
import android.content.SharedPreferences;

public class SessionHelper {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String PREF = "session";

    public SessionHelper(Context context){
        sharedPreferences = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void putString(String key, String value){
        doEdit();
        editor.putString(key, value);
        doCommit();
    }

    public void putInt(String key, int value){
        doEdit();
        editor.putInt(key, value);
        doCommit();
    }

    public void putFloat(String key, float value){
        doEdit();
        editor.putFloat(key, value);
        doCommit();
    }

    public void putBoolean(String key, boolean value){
        doEdit();
        editor.putBoolean(key, value);
        doCommit();
    }

    public String getString(String key, String value){
        return sharedPreferences.getString(key, value);
    }

    public int getInt(String key, int value){
        return sharedPreferences.getInt(key, value);
    }

    public float getFloat(String key, float value){
        return sharedPreferences.getFloat(key, value);
    }

    public boolean getBoolean(String key, boolean value){
        return sharedPreferences.getBoolean(key, value);
    }

    public boolean has(String key){
        return sharedPreferences.contains(key);
    }

    private void doEdit(){
        if(editor == null){
            editor = sharedPreferences.edit();
        }
    }

    private void doCommit(){
        if(editor != null){
            editor.commit();
            editor = null;
        }
    }

}
