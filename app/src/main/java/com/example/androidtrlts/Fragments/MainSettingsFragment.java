package com.example.androidtrlts.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceFragmentCompat;

import androidx.preference.SwitchPreference;

import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.FileList;
import com.example.androidtrlts.Utils.Route;

import java.io.File;
public class MainSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preference, rootKey);

        SwitchPreference theme = findPreference("pref_main_theme");
        SwitchPreference bin = findPreference("pref_bin");
        SwitchPreference mode = findPreference("pref_text_recog_mode");
        CheckBoxPreference exit = findPreference("pref_confirm_on_exit");

        if(theme.isChecked()){
            theme.setSummary("Dark mode");
        }else{
            theme.setSummary("Light mode");
        }

        if(bin.isChecked()){
            bin.setSummary("Enabled");
        }else{
            bin.setSummary("Disabled");
        }

        if(mode.isChecked()){
            mode.setSummary("On-cloud - Recognizes and identifies a broad range of languages and special characters");
        }else{
            mode.setSummary("On-device - Recognizes latin characters (results are less precise)");
        }

        exit.setOnPreferenceClickListener(preference -> {
            return false;
        });


        theme.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isVibrateOn = (Boolean) newValue;
            if(isVibrateOn){
                theme.setSummary("Dark mode");
            }else{
                theme.setSummary("Light mode");
            }

            return true;
        });

        mode.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean isVibrateOn = (Boolean) newValue;
            if(isVibrateOn){
                mode.setSummary("On-cloud - Recognizes and identifies a broad range of languages and special characters");
            }else{
                mode.setSummary("On-device - Recognizes latin characters (results are less precise)");
            }
            return true;
        });

        bin.setOnPreferenceClickListener(preference -> {

            if(bin.isChecked()){
                bin.setSummary("Enabled");
                bin.setChecked(true);
            }else{
                bin.setChecked(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Disable bin?")
                        .setMessage("All the files and folders in the bin will be deleted.").setCancelable(false)
                        .setPositiveButton("disable", (dialog, which) -> {
                            bin.setSummary("Disabled");
                            bin.setChecked(false);
                            try {
                                File file = new File(Route.parent + "/.bin");
                                if(file.exists()){
                                    FileHelper.deleteRecursive(file);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
            return true;
        });


    }

}

