package com.example.androidtrlts.Utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Util {
    public static final String PACKAGE = "com.example.androidtrlts";

    public static final int READ_EXTERNAL_STORAGE = 1;
    public static final int WRITE_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_IMAGE_CAPTURE = 3;
    public static final int REQUEST_IMAGE_SELECT = 4;
    public static final int REQUEST_IMAGE_URL = 5;
    public static final int SHARE_REQUEST_CODE = 6;
    public static final int REQUEST_AUDIO = 7;

    public final static String SAMPLE_CROPPED_IMG_NAME = "SampleCropImg";

    public static void showKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void closeKeyboard(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void copyToClipBoard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("CopiedText", text);
        if (clip == null) {
            Toast.makeText(context, "Unable to copy text!", Toast.LENGTH_SHORT).show();
            return;
        }
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Text has been copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    public static void showSnackBarUndo(View view, String message, int bgColor, Service service) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", v -> service.callback());
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(bgColor);
        snackbar.show();
    }

    public static void showSnackBar(View view, String message, int bgColor) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        sbView.setBackgroundColor(bgColor);
        snackbar.show();
    }

    public static String html2text(String html) {
        if(html==null)
            return html;
        Document document = Jsoup.parse(html);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));//makes html() preserve linebreaks and spacing
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n").replaceAll("&nbsp;"," ");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    public enum Storage {
        INTERNAL("internal", 0),
        EXTERNAL("external", 1);


        private String stringValue;
        private int intValue;
        private Storage(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public int toInt(){return intValue;}
    }

    public enum Order {
        ASC("ascending", 0),
        DESC("descending", 1);


        private String stringValue;
        private int intValue;
        private Order(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public int toInt(){return intValue;}
    }

    public enum Property {
        NAME("name", 0),
        DATE("date", 1);


        private String stringValue;
        private int intValue;
        private Property(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public int toInt(){return intValue;}
    }

    public enum Type {
        FILE("file", 0),
        FOLDER("folder", 1);


        private String stringValue;
        private int intValue;
        private Type(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public int toInt(){return intValue;}
    }

    public enum Action {
        OPEN("open", 0),
        CLOSE("close", 1);


        private String stringValue;
        private int intValue;
        private Action(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public int toInt(){return intValue;}
    }


    public static String removeTrailingChar(String str, String toRemove){
        if (str.endsWith(toRemove))
            return str.substring(0,str.length()-1);
        else
            return str;
    }

    public static String getLastCharsFromPrev(String str, String delimiter){
       return str.substring(str.lastIndexOf(delimiter)+1);
    }

    public static String getCharsFromPrev(String str, String delimiter){
        return str.substring(0, str.lastIndexOf(delimiter)+1);
    }

    public static String toCapitalizeString(String str){
      return  str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String getDate(long date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/M/dd hh:mm:ss a");
        String strDate = dateFormat.format(date);
        return strDate;
    }
}

