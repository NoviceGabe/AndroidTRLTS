package com.example.androidtrlts.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    public static boolean isNameValid(String name){
        String allowedChars = "^[a-z-0-9- _ ( ) /]+$";
        Pattern pattern = Pattern.compile(allowedChars, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);

        if(!matcher.matches()){
            return false;
        }

        return true;
    }
}
