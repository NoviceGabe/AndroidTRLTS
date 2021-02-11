package com.example.androidtrlts.Adapters;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.androidtrlts.Model.Item;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.Util;

import java.util.List;

public class ItemAdapter extends BaseAdapter {
    private Context context;
    private List<Item> items;
    private LayoutInflater inflater;
    private int layoutId;

    public ItemAdapter(Context context, List<Item> items, int layoutId) {
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        if (items != null && items.size() > 0) {
            return  items.size();
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }

        ImageView fileIcon;
        TextView fileName;
        TextView fileModified;
        TextView fileChildrenCount;

        fileIcon = view.findViewById(R.id.iv_file_icon);
        fileName = view.findViewById(R.id.tv_file_name);
        fileModified = view.findViewById(R.id.tv_file_modified);
        fileChildrenCount = view.findViewById(R.id.tv_file_count_item);

        final Item item = (Item) getItem(position);;
        Uri image = Uri.parse("android.resource://"+context.getPackageName()+"/"+R.drawable.ic_file_outline_24dp);

        if(item.getImageResource() > 0){
            Uri uri =  Uri.parse("android.resource://"+context.getPackageName()+"/"+item.getImageResource());
            image = (uri != null)? uri: image;
        }
        fileIcon.setImageURI(null);
        fileIcon.setImageURI(image);
        String suffix = (item.getChildCount() > 1)?"s":"";
        fileName.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        fileName.setText(item.getName());

        fileModified.setText(Util.getDate(item.getDateModified()));
       if(item.getType() == Types.FOLDER){
            fileChildrenCount.setText(String.format("%s item%s", item.getChildCount(), suffix));
        }

        return view;
    }

    public enum Types {
        FILE("file", 0),
        FOLDER("folder", 1);


        private String stringValue;
        private int intValue;
        private Types(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public int toInt(){return intValue;}
    }
}
