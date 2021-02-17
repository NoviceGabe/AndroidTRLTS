package com.example.androidtrlts.Fragments;


import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.androidtrlts.R;

import java.util.ArrayList;


public class HelpFragment extends Fragment {
    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.listview, container, false);
        listView = mView.findViewById(R.id.listView);

        ArrayList<String> text = new ArrayList<>();
        text.add("On the home page, tap the <img src=\"capture\"> icon to take picture or tap <img src=\"insert_image\"> icon to open an image from the Gallery. You can also get image from url using the <img src=\"url\"> icon");
        text.add("Make sure that the image is in good quality for best result. Then crop the portion of the text image you want to convert to text.");
        text.add("Wait while extracting text on image.");
        text.add("Tap <img src=\"preview_image\"> icon to preview the source image of the text.");
        text.add("Tap <img src=\"copy\"> icon to copy extracted text to the clipboard.");
        text.add("Tap <img src=\"translate\"> icon to translate text to target language.");
        text.add("Tap <img src=\"audio\"> icon to convert text to audible sound.");
        text.add("Tap <img src=\"save\"> icon to save the text into a text document. You can also save it in other format such as html.");
        text.add("You can search a document on the homepage by using the <img src=\"search\"> icon");
        text.add("To create a folder to organize files, tap <img src=\"create_folder\"> icon");
        text.add("Tap any scanned item to view its content on the text editor.");
        text.add("Long press on any item to show the context menu. Scanned items have options rename/delete/share/details. Folders have options rename/delete/details.");
        text.add("On the main settings, you can change the app theme by switching theme to light mode or dark mode. To move deleted files to the bin toggle bin to enabled ");
        text.add("To export a document to pdf format select the export item on editor options menu.");
        text.add("To share a text or a document select the share item on editor options menu.");
        text.add("You need to sign in first to backup or sync your documents to the cloud");
        text.add("To backup a document to the cloud select the backup item on editor options menu.");
        text.add("You can sync documents to the cloud.");
        text.add("To sync documents to the cloud automatically check the auto-sync in the editor settings.");
        text.add("To sync documents from cloud to your local device check the auto-sync in the main settings");
        text.add("You can style and format text in the text editor using the editor’s bottom toolbar. You need to save it as an html file to see the effect. ");
        text.add("On the editor settings, you can change the appearance  of the text editor on the appearance preference.");
        text.add("You can change the mode of the text editor to read-only or read & write.");
        text.add("Check the show keyboard on editor startup to show keyboard right after launching the text editor.");
        text.add("You can change the source language detection to auto or manual in the text source language option.");
        text.add("You can switch the text recognition mode to on-device or on-cloud in the settings.");
        text.add("To enable auto save check the auto save checkbox.");


        HtmlAdapter htmlAdapter = new HtmlAdapter(text, getActivity(), R.layout.item1);
        listView.setAdapter(htmlAdapter);
        return mView;
    }


    public class HtmlAdapter extends BaseAdapter {
        private ArrayList<String> list;
        private Context context;
        private int resource;


        public HtmlAdapter(ArrayList<String> list, Context context, int resource) {
            this.list = list;
            this.context = context;
            this.resource = resource;
        }


        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return  list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(context).inflate(resource, parent, false);
            }

            TextView description = view.findViewById(R.id.help_description);
            description.setTextColor(getContext().getResources().getColor(R.color.textColor));
            String item = (String) getItem(position);

            description.setText(Html.fromHtml(item, source -> {
                int resourceId = getResources().getIdentifier(source, "drawable", getActivity().getPackageName());
                Drawable drawable = getActivity().getResources().getDrawable(resourceId);
                drawable.setBounds(0, 0, 100, 100);
                return drawable;
            }, null));

            return view;
        }

    }

}

