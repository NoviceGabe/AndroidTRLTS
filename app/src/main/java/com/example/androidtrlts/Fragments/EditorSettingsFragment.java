package com.example.androidtrlts.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.CheckBoxPreference;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;

public class EditorSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.text_editor_preference, rootKey);

        final CheckBoxPreference pref_keyboard = findPreference("pref_keyboard");
        SwitchPreference mode = findPreference("pref_mode");
        CheckBoxPreference auto_save = findPreference("pref_auto_save");
        DropDownPreference source_lang = findPreference("pref_source_lang");
        Preference engine = findPreference("pref_engine");
        Preference reset_pref = findPreference("reset_pref");

        SessionHelper sessionManager = new SessionHelper(getActivity());

        String engineName = sessionManager.getSessionString("engine", "");
        switch (engineName){
            case "com.google.android.tts":
                engineName = "Google Text-to-Speech";
                break;
            case "com.svox.pico":
                engineName = "Pico Text-to-Speech";
                break;
            case "com.samsung.SMT":
                engineName = "Samsung Text-to-Speech";

        }
        engine.setSummary(engineName);


        if(mode.isChecked()){
            mode.setSummary("Read & write");
        }else{
            mode.setSummary("Read-only");
        }


        mode.setOnPreferenceChangeListener((preference, newValue) -> {
            if(mode.isChecked()){
                if(pref_keyboard.isChecked()){
                    pref_keyboard.setChecked(false);
                }
                mode.setSummary("Read-only");
                Log.d("pref_mode", "read only");
            }else{
                mode.setSummary("Read & write");
                Log.d("pref_mode", "read and write");

            }

            return true;
        });
        //   persistCheckBoxState(mode, pref_keyboard);
        CharSequence sourceLangCurrentText = source_lang.getEntry();
        source_lang.setSummary(sourceLangCurrentText);
        source_lang.setOnPreferenceChangeListener((preference, newValue) -> {
            String sourceLang;
            switch (Integer.valueOf(String.valueOf(newValue))){
                case 1:
                    sourceLang = "Auto-detect";
                    break;
                default:
                    sourceLang = "Manual";
            }
            source_lang.setSummary(sourceLang);

            return true;
        });

        reset_pref.setOnPreferenceClickListener(preference -> {
            sessionManager.clearAllSession();
            if(mode.isChecked()){
                mode.setChecked(false);
            }
            if(auto_save.isChecked()){
                auto_save.setChecked(false);
            }
            if(pref_keyboard.isChecked()){
                pref_keyboard.setChecked(false);
            }
            Toast.makeText(getActivity(), "Preferences have been reset!", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void persistCheckBoxState(CheckBoxPreference enable_edit, CheckBoxPreference pref_keyboard){
        if(pref_keyboard.isChecked()){
            pref_keyboard.setChecked(false);

        }
    }


}
