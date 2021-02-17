package com.example.androidtrlts.Utils;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.androidtrlts.Activities.MainActivity;
import com.example.androidtrlts.Adapters.ItemAdapter;
import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.Model.Item;
import com.example.androidtrlts.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FileList {
    private ListView listView;
    private TextView empty;
    private ItemAdapter itemAdapter;
    private java.util.List<Item> items;

    public static String currentDirPath;
    public static String pathOriginRoot;
    public static String currentFolderName;

    private static int storage;

    private SessionHelper session;
    private static boolean stopDirAccess = true;

    public static final String SYMLINK = "/storage/emulated/";
    public static boolean allowOutsideRootAccess = false;
    public static Util.Action action;

    private Activity activity;

    public FileList(Activity activity, ListView listView, TextView empty){
        this.activity = activity;
        this.listView = listView;
        this.empty = empty;
        this.items = new ArrayList<>();

        session = new SessionHelper(Objects.requireNonNull(activity));
        init();
    }

    public void reloadListView(String currentDirPath, String filter){
        if(items != null){
            items.clear(); // remove old data
        }
        java.util.List<Item> newItems = load(currentDirPath, filter);
        sort(newItems, MainActivity.order, MainActivity.property, false);
        attach(newItems);
    }

    public static java.util.List<Item> load(String currentDirPath, String filter){
        return FileHelper.getItems(currentDirPath, filter);
    }

    public void attach(java.util.List<Item> items){
        if(items != null && items.size() > 0){
            this.items.addAll(items);
            itemAdapter = new ItemAdapter(activity, items, R.layout.item);
            listView.setAdapter(itemAdapter);
            itemAdapter.notifyDataSetChanged();


            if(listView.getVisibility() == View.GONE){
                listView.setVisibility(View.VISIBLE);
                empty.setVisibility(View.GONE);
            }
        }else{
            listView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
    }

    private void init(){
        storage = session.getSessionInt("storage", Util.Storage.INTERNAL.toInt());

        if(storage == Util.Storage.EXTERNAL.toInt() && FileHelper.getExternalStorage() != null){
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
                Route.parent = FileHelper.getExternalStorage().toString();
            }
        }else{
            Route.parent = FileHelper.getInternalStorage().toString();
        }
        if(currentDirPath == null){
            currentDirPath = Route.getFullPath()+"/"; // full path
        }

        if(pathOriginRoot == null){
            pathOriginRoot = Route.ROOT+"/"; // path started with root
        }

        if(currentFolderName == null){
            currentFolderName = Route.ROOT;
        }

        try {
            File parent = new File(Route.parent);
            FileHelper.createDirectory(parent, Route.ROOT);
        } catch (Exception exception) {
            Log.d("storage", exception.getMessage());
        }

    }

    public static void allowDirAccess(){
        stopDirAccess = false;
    }

    public static void StopDirAccess(){
        stopDirAccess = true;
    }

    public static  boolean dirAccess(){
        return stopDirAccess;
    }

    public static void next(String itemName){
        currentFolderName = itemName;
        currentDirPath += currentFolderName+"/";
        pathOriginRoot += currentFolderName+"/";
    }

    public static void previous(){
        String target = Util.getCharsFromPrev(Util.removeTrailingChar(currentDirPath, "/"), "/");
        File parent = new File(target);
        currentDirPath = parent.getPath()+"/";
        currentFolderName = parent.getName();
        pathOriginRoot = Util.getCharsFromPrev(Util.removeTrailingChar(pathOriginRoot, "/"),"/");

        if(Util.removeTrailingChar(currentDirPath,"/").equals(Route.parent)){
            if(storage == Util.Storage.EXTERNAL.toInt()){
                currentFolderName = Util.toCapitalizeString(Util.Storage.EXTERNAL.toString()+" Storage");
                pathOriginRoot =  Util.toCapitalizeString(Util.Storage.EXTERNAL.toString())+" Storage/";
            }else{
                currentFolderName = Util.toCapitalizeString(Util.Storage.INTERNAL.toString()+" Storage");
                pathOriginRoot = Util.toCapitalizeString(Util.Storage.INTERNAL.toString())+" Storage/";
            }
        }

    }

    public static void home(){
        currentDirPath = Route.getFullPath()+"/";
        pathOriginRoot = Route.ROOT+"/";
        currentFolderName = Route.ROOT;

    }

    public static void sort(java.util.List<Item> item, Util.Order order, Util.Property property, boolean ignoreType){
        if(item != null){
            Collections.sort(item, (o1, o2) -> {
                int type = -1;

                if(!ignoreType){
                    type = o2.getType().compareTo(o1.getType()); //return item folder type
                    //if two items has the same type compare their names
                    //then sort alphabetically asc/desc order
                    if(type == 0){

                        if (order == Util.Order.DESC) {
                            if (property == Util.Property.DATE) {
                                return Long.compare(o2.getDateModified(), o1.getDateModified());
                            }
                            return o2.getName().compareToIgnoreCase(o1.getName());
                        }else{
                            if (property == Util.Property.DATE) {
                                return Long.compare(o1.getDateModified(), o2.getDateModified());
                            }
                            return o1.getName().compareToIgnoreCase(o2.getName());
                        }

                    }
                }else{
                    if (order == Util.Order.DESC) {
                        if (property == Util.Property.DATE) {
                            return Long.compare(o2.getDateModified(), o1.getDateModified());
                        }
                        return o2.getName().compareToIgnoreCase(o1.getName());
                    }else{
                        if (property == Util.Property.DATE) {
                            return Long.compare(o1.getDateModified(), o2.getDateModified());
                        }
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                }
                return type;
            });
        }
    }
}
