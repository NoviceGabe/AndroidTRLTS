package com.example.androidtrlts.Fragments;

import android.os.Bundle;

import androidx.preference.DropDownPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.FontListParser;
import com.rarepebble.colorpicker.ColorPreference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditorSettingsAppearanceFragment extends PreferenceFragmentCompat {
    ColorPreference fontColor;
    ColorPreference backGroundColor;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.style_preference, null);
        SeekBarPreference font_size =  findPreference("pref_font_size");
        fontColor =  findPreference("pref_color_picker");
        backGroundColor = findPreference("pref_color_picker_background");
        DropDownPreference theme = findPreference("pref_theme");
        CharSequence themeCurrentText = theme.getEntry();
        String themeCurrentValue = theme.getValue();
        DropDownPreference fontFamily = findPreference("pref_font_family");

        final List<FontListParser.SystemFont> fonts = FontListParser.safelyGetSystemFonts();
        String[] items = new String[fonts.size()];
        String[] values = new String[fonts.size()];
        for(int i = 0; i < fonts.size(); i++){
            items[i] = fonts.get(i).name.replaceAll("-"," ");
            values[i] = fonts.get(i).name;
        }

        Arrays.sort(items, Collections.reverseOrder());
        Arrays.sort(values, Collections.reverseOrder());

        fontFamily.setEntries(items);
        fontFamily.setEntryValues(values);

        CharSequence fontFamilyCurrentText = fontFamily.getEntry();

        fontFamily.setSummary(fontFamilyCurrentText);
        theme.setSummary(themeCurrentText);


        if(themeCurrentValue != null && Integer.valueOf(themeCurrentValue) != 1){
            fontColor.setEnabled(false);
            backGroundColor.setEnabled(false);
        }

        font_size.setSummary(font_size.getValue()+"px");
        font_size.setOnPreferenceChangeListener((preference, newValue) -> {
            final int progress = Integer.valueOf(String.valueOf(newValue));
            font_size.setSummary(progress+"px");
            return true;
        });

        fontColor.setOnPreferenceClickListener(preference -> {
            onDisplayPreferenceDialog(preference);
            return true;
        });


        backGroundColor.setOnPreferenceClickListener(preference -> {
            onDisplayPreferenceDialog(preference);
            return true;
        });

        theme.setOnPreferenceChangeListener((preference, newValue) -> {
            String stheme;
            boolean enableTheme = true;
            switch (Integer.valueOf(String.valueOf(newValue))){
                case 1:
                    stheme = "custom";
                    enableTheme = false;
                    break;
                case 2:
                    stheme = "lighter";
                    break;
                case 3:
                    stheme = "one light";
                    break;
                case 4:
                    stheme = "github";
                    break;
                case 5:
                    stheme = "oceanic";
                    break;
                case 6:
                    stheme = "monokai pro";
                    break;
                case 7:
                    stheme = "ark dark";
                    break;
                case 8:
                    stheme = "dracula";
                    break;
                case 9:
                    stheme = "darker";
                    break;
                default:
                    stheme = "default";
            }

            if(enableTheme){
                fontColor.setEnabled(false);
                backGroundColor.setEnabled(false);
            }else{
                fontColor.setEnabled(true);
                backGroundColor.setEnabled(true);
            }
            theme.setSummary(stheme);

            return true;
        });

        fontFamily.setOnPreferenceChangeListener((preference, newValue) -> {
            String value = String.valueOf(newValue).replaceAll("-"," ");
            fontFamily.setSummary(value);
            return true;
        });

    }

    public void onDisplayPreferenceDialog(Preference pref){
        if(pref instanceof ColorPreference){
            ((ColorPreference)pref).showDialog(this, 0);

        }else{
            super.onDisplayPreferenceDialog(pref);
        }
    }

}

