package com.example.androidtrlts.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class CustomBaseAdapter extends BaseAdapter {
    private Context context;
    protected List<?> items;
    private LayoutInflater inflater;
    protected int layoutId;

    public CustomBaseAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
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

    public abstract View getView(View view, int pos);

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if(view == null){
            view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }

        return getView(view, position);
    }

}
