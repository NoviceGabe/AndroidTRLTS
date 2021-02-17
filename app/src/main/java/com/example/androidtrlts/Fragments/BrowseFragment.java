package com.example.androidtrlts.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.androidtrlts.Activities.MainActivity;
import com.example.androidtrlts.Activities.TextEditorActivity;
import com.example.androidtrlts.Adapters.ItemAdapter;
import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.Model.Item;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.FileList;
import com.example.androidtrlts.Utils.IFetchContent;
import com.example.androidtrlts.Utils.Route;
import com.example.androidtrlts.Utils.Util;
import com.example.androidtrlts.Utils.Validator;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment {
    private View mView;
    private List<Item> items;
    private Item item;
    private FileList fileList;

    public static  ImageView backBtn;
    public static TextView currentPathView;
    private ListView listView;
    private TextView empty;
    private SwipeRefreshLayout refreshLayout;
    private String filter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.main_content, container, false);
        currentPathView = mView.findViewById(R.id.current_path);
        backBtn = mView.findViewById(R.id.back);
        empty = mView.findViewById(R.id.empty);
        listView = (ListView) mView.findViewById(R.id.listView);
        refreshLayout = mView.findViewById(R.id.refreshLayout);

        fileList = new FileList(getActivity(), listView, empty);

        // toolbar
        ((MainActivity)getActivity()).getSupportActionBar()
                .setTitle(FileList.currentFolderName.replace(Route.ROOT, "Home"));
        // bread crumbs
        currentPathView.setText(FileList.pathOriginRoot.replace(Route.ROOT, "Home"));

        // hide back button if the current directory is root and access outside root is not permissible
        if(!FileList.allowOutsideRootAccess &&
                Util.removeTrailingChar(FileList.currentDirPath, "/").equals(Route.getFullPath())){
            backBtn.setVisibility(View.GONE);
        }

        Bundle bundle = getArguments();
        if(bundle!=null){
            if(bundle.containsKey("filter")){
                filter = bundle.getString("filter");
            }
        }

        items = FileList.load(FileList.currentDirPath, filter);
        FileList.sort(items, MainActivity.order, MainActivity.property, false);
        fileList.attach(items);

        if(FileList.action == Util.Action.CLOSE){
            back();
            FileList.action = Util.Action.OPEN;
        }

        backBtn.setOnClickListener(v -> {
            back();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(items != null && items.size() > 0){
                if(items.get(position).getType().toString().equals(ItemAdapter.Types.FOLDER.toString())){

                    FileList.next(items.get(position).getName()); // change directory
                    fileList.reloadListView(FileList.currentDirPath, filter); // reload list view with new  items

                    // toolbar
                    ((MainActivity)getActivity()).getSupportActionBar().setTitle(FileList.currentFolderName);
                    // bread crumbs
                    currentPathView.setText(FileList.pathOriginRoot.replace(Route.ROOT, "Home"));

                    FileList.allowDirAccess();
                    backBtn.setVisibility(View.VISIBLE);
                }else{
                    String name = FileList.currentDirPath+items.get(position).getName();
                    File file = new File(name);
                    String text = FileHelper.read(file);
                    String title = FileHelper.getName(file);

                    TextEditorActivity.filePath = file.toString();
                    String dir = file.toString().substring(0, file.toString().lastIndexOf("/"));
                    String imagePath = dir +"/"+title+".jpg";

                    TextEditorActivity.image = BitmapFactory.decodeFile(imagePath);

                    Intent intent = new Intent(getActivity(), TextEditorActivity.class);
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("extractedText", text);
                    bundle1.putString("title", title);
                    intent.putExtras(bundle1);
                    getActivity().startActivity(intent);
                }
            }
        });
        registerForContextMenu(listView);

        refreshLayout.setOnRefreshListener(() -> {
            fileList.reloadListView(FileList.currentDirPath, filter); // reload list view
            refreshLayout.setRefreshing(false);
        });

        return mView;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        this.item = items.get(info.position); //get item
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.rename:
                renameItem();
                return true;

            case R.id.delete:
                deleteItem();
                return true;

            case R.id.share:
                File sfile = new File(FileList.currentDirPath + this.item.getName());
                FileHelper.shareFile(getActivity(), sfile, "*/*");
                return true;

            case R.id.details:
                File file = new File(FileList.currentDirPath+ this.item.getName());
                Uri uri = Uri.fromFile(file);
                String fileType = "\n\nMimeType: " + FileHelper.getMimeType(file);
                String fileSize = FileHelper.getFileSize(file.length());
                String itemCount = "";

                if(this.item.getType().equals(ItemAdapter.Types.FOLDER.toString())){
                    long count =  this.item.getChildCount();
                    String suffix = "";
                    if(count > 1){
                        suffix = "s";
                    }
                    itemCount = "\n\nItem"+suffix+": " + count;
                }

                DateFormat dateFormat = new SimpleDateFormat("yyyy/M/dd hh:mm:ss a");
                String strDate = dateFormat.format(this.item.getDateModified());

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.customDialog);

                builder.setTitle("Details").setMessage(
                        "Name: " + this.item.getName() +
                                itemCount +
                                fileType +
                                "\n\nSize: " + fileSize +
                                "\n\nPath: " + file.toString() +
                                "\n\nLast modified: " + strDate).setCancelable(true)
                        .setPositiveButton("Ok", (dialog, which) -> {});

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
        }
        return super.onContextItemSelected(item);
    }


    public void back(){
        String target = Util.getCharsFromPrev(Util.removeTrailingChar(FileList.currentDirPath, "/"), "/");

        if(Util.removeTrailingChar(target, "/") .equals(Route.getFullPath()) && !FileList.allowOutsideRootAccess){
            backBtn.setVisibility(View.GONE);
        }

        if(FileList.dirAccess()){
            return;
        }

        FileList.previous();
        String targetParent = Util.getCharsFromPrev( Util.removeTrailingChar(FileList.currentDirPath, "/"),"/");

        ((MainActivity)getActivity()).getSupportActionBar()
                .setTitle(FileList.currentFolderName.replace(Route.ROOT, "Home"));
        // bread crumbs
        currentPathView.setText(FileList.pathOriginRoot.replace(Route.ROOT, "Home"));

        fileList.reloadListView(FileList.currentDirPath, filter);

        if(items == null){
            FileList.StopDirAccess();
            backBtn.setVisibility(View.GONE);
        }

        if(targetParent.equals(FileList.SYMLINK)){
            FileList.StopDirAccess();
            backBtn.setVisibility(View.GONE);
        }
    }

    private void renameItem() {
        Activity activity = getActivity();
        String oldName = item.getName();
        File image = null;

        if(this.item.getType().toString().equals(ItemAdapter.Types.FILE.toString())){
            oldName = item.getName().substring(0,item.getName().lastIndexOf("."));
            image = new File(FileList.currentDirPath+oldName+".jpg");
        }

        File file = new File(FileList.currentDirPath+item.getName()); // current file

        String finalOldName = oldName;
        File finalImage = image;
        ((MainActivity) activity).alertDialogInput("Rename", oldName, R.layout.text_input, (IFetchContent<String>) name -> {
            File file1 = new File(FileList.currentDirPath+name);

            if(this.item.getType().toString().equals(ItemAdapter.Types.FILE.toString())){
                file1 = new File(FileList.currentDirPath+name+".txt"); // same file but different name
            }

            if(name.isEmpty()){
                Toast.makeText(getActivity(), "Please enter a name", Toast.LENGTH_SHORT).show();
            }else if(!Validator.isNameValid(name)){
                Toast.makeText(getActivity(), "Invalid name!", Toast.LENGTH_SHORT).show();
            }else if(file1.exists() || file1.isDirectory()){
                Toast.makeText(getActivity(), "Name already exists!", Toast.LENGTH_SHORT).show();
            }else{
                final View view = mView.findViewById(R.id.main_content);

                try {
                    File renamedFile = FileHelper.renameFile(file, name);
                    if(renamedFile != null){
                        File renamedImage = null;
                        if(finalImage != null && finalImage.exists()){
                            renamedImage = FileHelper.renameFile(finalImage, name);
                            if(renamedImage != null){
                                Util.showSnackBar(view,  "\"" + finalOldName + "\" has been renamed to \"" + name + "\"",
                                        getResources().getColor(R.color.success));
                                ((MainActivity) activity).loadFragment(MainActivity.FRAGMENT_STATE_REPLACE, null);
                                return;
                            }
                            throw new Exception("Unable to rename \""+ name +"\"");
                        }else{
                            Util.showSnackBar(view,  "\"" + finalOldName + "\" has been renamed to \"" + name + "\"",
                                    getResources().getColor(R.color.success));
                            ((MainActivity) activity).loadFragment(MainActivity.FRAGMENT_STATE_REPLACE, null);
                        }
                        return;
                    }

                    throw new Exception("Unable to rename \""+ name +"\"");
                }catch (Exception e){
                    Util.showSnackBar(view,  e.getMessage(),
                            getResources().getColor(R.color.error));
                }
            }
        });
    }

    private void deleteItem(){
        File file = new File(FileList.currentDirPath+item.getName());

        if(file.exists()){

            File image = null;

            if(this.item.getType().toString().equals(ItemAdapter.Types.FILE.toString())){
                String name = item.getName().substring(0,item.getName().lastIndexOf("."));
                image = new File(FileList.currentDirPath+name+".jpg");
            }

            String message = "Are you sure you want to";
            String message1 = "This folder contains sub-item(s), delete it anyway?";
            String mode = "delete";
            String type = "this file";

            if(file.isDirectory()){
                type =  "this folder";
            }

            message = message + " " + mode + " " + type+"?";

            String finalMessage = message1;
            File finalImage = image;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.customDialog);
            builder.setMessage(message);

            View  view = getActivity().findViewById(R.id.main_content);

            builder.setCancelable(false).setPositiveButton("Yes", (dialog1, which1) -> {

                if(this.item.getType().toString().equals(ItemAdapter.Types.FOLDER.toString())){
                    if(FileHelper.getSize(file) > 0){
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity(), R.style.customDialog);
                        builder1.setMessage(finalMessage);

                        builder1.setCancelable(false).setPositiveButton("Yes", (dialog2, which2) -> {
                            try {
                                FileHelper.deleteFile(file);
                                Util.showSnackBar(view,  "File successfully deleted.",
                                        getResources().getColor(R.color.success));
                                ((MainActivity) getActivity()).loadFragment(MainActivity.FRAGMENT_STATE_REPLACE, null);
                            } catch (Exception e) {
                                Util.showSnackBar(view,  e.getMessage(),
                                        getResources().getColor(R.color.error));
                            }
                        });

                        builder1.setNegativeButton("No", (dialog2, which2) -> dialog2.cancel());

                        AlertDialog alertDialog = builder1.create();
                        alertDialog.show();
                    }else{
                        try {
                            FileHelper.deleteFile(file);
                            Util.showSnackBar(view,  "File successfully deleted.",
                                    getResources().getColor(R.color.success));
                            ((MainActivity) getActivity()).loadFragment(MainActivity.FRAGMENT_STATE_REPLACE, null);
                        } catch (Exception e) {
                            Util.showSnackBar(view,  e.getMessage(),
                                    getResources().getColor(R.color.error));
                        }
                    }

                }else{
                    try {
                        FileHelper.deleteFile(file);
                        if(finalImage != null){
                            FileHelper.deleteFile(finalImage);
                        }
                        Util.showSnackBar(view,  "File successfully deleted.",
                                getResources().getColor(R.color.success));
                        ((MainActivity) getActivity()).loadFragment(MainActivity.FRAGMENT_STATE_REPLACE, null);

                    } catch (Exception e) {
                        Util.showSnackBar(view,  e.getMessage(),
                                getResources().getColor(R.color.error));
                    }
                }


            });

            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

}
