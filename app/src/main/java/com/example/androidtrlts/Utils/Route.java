package com.example.androidtrlts.Utils;

import com.example.androidtrlts.Helpers.FileHelper;

public class Route {
    public static String parent = FileHelper.getInternalStorage().toString();
    public static final String ROOT = "AndroidTRLTS";
    public static String getFullPath(){
        return parent + "/" + ROOT;
    }

}
