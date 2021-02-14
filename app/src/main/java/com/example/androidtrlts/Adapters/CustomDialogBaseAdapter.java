package com.example.androidtrlts.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.DialogItem;

import java.util.ArrayList;

public class CustomDialogBaseAdapter extends BaseAdapter {
    ArrayList<DialogItem> name;
    Context context;
    LayoutInflater inflater;

    public CustomDialogBaseAdapter(ArrayList<DialogItem> name, Context context) {
        this.name = name;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return name.size();
    }

    @Override
    public DialogItem getItem(int position) {
        return name.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.dialog_list_item, parent, false);
        }

        DialogItem currentItem = getItem(position);
        TextView textViewItemName = (TextView) view.findViewById(R.id.dialog_item_name);
        ImageView imageView = (ImageView) view.findViewById(R.id.dialog_item_icon);

        Uri imgUri;
        switch (currentItem.getItemType().toLowerCase()){
            case "backup_google_drive":
                imgUri = Uri.parse("android.resource://com.example.androidtrlts/"+R.drawable.ic_google_drive_24dp_black);
                break;
            case "backup":
                imgUri = Uri.parse("android.resource://com.example.androidtrlts/"+R.drawable.ic_baseline_backup_24);
                break;
            case "pdf":
                imgUri = Uri.parse("android.resource://com.example.androidtrlts/"+R.drawable.ic_pdf_outline_24dp);
                break;
            case "share_text":
                imgUri = Uri.parse("android.resource://com.example.androidtrlts/"+R.drawable.ic_font_black_24dp);
                break;
            case "share_any":
                imgUri = Uri.parse("android.resource://com.example.androidtrlts/"+R.drawable.ic_file_share_24dp_black);
                break;
            case "doc":
            case "docx":
                imgUri = Uri.parse("android.resource://com.example.androidtrlts/"+R.drawable.ic_doc_outline_24dp);
                break;
            default:
                imgUri =Uri.parse("android.resource://com.example.androidtrlts/"+R.drawable.ic_panorama_black_24dp);
        }

        textViewItemName.setText(currentItem.getItemName());
        imageView.setImageURI(null);
        imageView.setImageURI(imgUri);
        int color = Color.parseColor("#000000"); //The color u want
        imageView.setColorFilter(color);

        return view;
    }
}
