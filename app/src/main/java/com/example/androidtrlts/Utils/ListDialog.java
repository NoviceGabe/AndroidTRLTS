package com.example.androidtrlts.Utils;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class ListDialog {
    private Context context;
    private AlertDialog.Builder alertDialog;
    private AlertDialog alert;
    private List<String> items;
    private int defaultItem = 0;

    public ListDialog(Context context){
        this.context = context;
        alertDialog = new AlertDialog.Builder(context);
        items = new ArrayList<>();
    }

    public void setTitle(String title){
        alertDialog.setTitle(title);
    }

    public void addItem(String item){
        items.add(item);
    }

    public void changeDefaultItem(int item){
        defaultItem = item;
    }

    public void show(IFetchContent fetch) {
        if(items.size() == 0){
            Toast.makeText(context, "No Items", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] toString = items.toArray(new String[0]);
        alertDialog.setSingleChoiceItems(toString, defaultItem, (dialog, which) ->
        {
            fetch.onFetch(which);
        });

        alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    public void dismiss(){
        alert.dismiss();
    }

}
