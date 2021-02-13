package com.example.androidtrlts.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.androidtrlts.Activities.MainActivity;
import com.example.androidtrlts.Activities.TextEditorActivity;
import com.example.androidtrlts.Adapters.ItemAdapter;
import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.Helpers.ImageHelper;
import com.example.androidtrlts.Helpers.OCRHelper;
import com.example.androidtrlts.Model.Item;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.FileList;
import com.example.androidtrlts.Utils.Route;
import com.example.androidtrlts.Utils.Util;
import com.example.androidtrlts.Utils.Validator;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DialogFragment extends androidx.fragment.app.DialogFragment  {
    private View mView;
    private List<Item> items;
    private FileList fileList;

    private ImageView backBtn;
    private TextView currentPathView;
    private ListView listView;
    private TextView empty;
    private SwipeRefreshLayout refreshLayout;
    private File file;

    private AlertDialog alertDialog;
    private TextEditorActivity activity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mView = inflater.inflate(R.layout.dialog_save_file_list_item, null);

        currentPathView = mView.findViewById(R.id.current_path);
        backBtn = mView.findViewById(R.id.back);
        empty = mView.findViewById(R.id.empty);
        listView = (ListView) mView.findViewById(R.id.listView);
        refreshLayout = mView.findViewById(R.id.refreshLayout);

        fileList = new FileList(getActivity(), listView, empty);
        String path = fileList.getPathOriginRoot().replace(Route.ROOT, "Home");
        currentPathView.setText(fileList.getPathOriginRoot().replace(Route.ROOT, "Home"));
        activity = ((TextEditorActivity) getActivity());
        // hide back button if the current directory is root and access outside root is not permissible
        if(!FileList.allowOutsideRootAccess &&
                Util.removeTrailingChar(FileList.currentDirPath, "/").equals(Route.getFullPath())){
            backBtn.setVisibility(View.GONE);
        }

        items = fileList.load(FileList.currentDirPath);
        FileList.sort(items, MainActivity.order, MainActivity.property, false);
        fileList.attach(items);

        backBtn.setOnClickListener(v -> {
            back();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(items.get(position).getType().toString().equals(ItemAdapter.Types.FOLDER.toString())){

                FileList.next(items.get(position).getName()); // change directory
                fileList.reloadListView(FileList.currentDirPath); // reload list view with new  items

                // bread crumbs
                currentPathView.setText(fileList.getPathOriginRoot().replace(Route.ROOT, "Home"));

                FileList.allowDirAccess();
                backBtn.setVisibility(View.VISIBLE);
            }

        });

        refreshLayout.setOnRefreshListener(() -> {
            fileList.reloadListView(FileList.currentDirPath); // reload list view
            refreshLayout.setRefreshing(false);
        });

        Bundle bundle = getArguments();
        String title = "New file";
        final String extension = ".txt";

        if(bundle!=null){
            if(bundle.containsKey("title")){
                title = bundle.getString("title");
            }
        }

        if(TextEditorActivity.filePath != null && !TextEditorActivity.filePath.isEmpty()){
            title = TextEditorActivity.filePath.substring(TextEditorActivity.filePath.lastIndexOf("/")+1,
                    TextEditorActivity.filePath.lastIndexOf("."));
            title = (title.length() > 50)? title.substring(0,50): title;
        }

        title = title + extension;

        TextView tvTitle = new TextView(getActivity());
        tvTitle.setPadding(20, 30, 20, 30);
        tvTitle.setTextSize(20f);
        tvTitle.setText("Save as");
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        builder.setCustomTitle(tvTitle);

        final EditText input = (EditText) mView.findViewById(R.id.input);
        input.setOnFocusChangeListener((v, hasFocus) -> input.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }));

        file = FileHelper.validateFileName(FileList.currentDirPath +"/"+title);
        String filename = file.toString();
        filename = filename.substring(filename.lastIndexOf("/")+1);
        input.setText(filename);

        input.requestFocus();

        builder.setView(mView);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String inputText =  input.getText().toString();
            //check if filename is valid
            if(!isFileNameValid(inputText)){
                return;
            }

            String name = FileHelper.removeExtension(inputText).trim() + extension;

            file = new File(FileList.currentDirPath +"/"+name);

            //Checks if the file already exists
            //If the file was already existed, the user will choose if he/she will overwrite the file or not
            //Otherwise if the file wasn't existed before then it will be saved as a new file
            if(file.exists()){
                final AlertDialog.Builder alert1 = new AlertDialog.Builder(getActivity());
                View mView1 = getLayoutInflater().inflate(R.layout.dialog_replace_file, null);

                alert1.setTitle("Rename file?");
                alert1.setMessage("You already have a file named \""+ inputText+
                        "\" in the destination folder.");

                Button btn_skip = mView1.findViewById(R.id.dialog_replace_file_cancel);
                Button btn_replace = mView1.findViewById(R.id.dialog_replace_file_ok);

                alert1.setView(mView1);
                final AlertDialog alertDialog1 = alert1.create();
                alertDialog1.setCanceledOnTouchOutside(false);

                btn_skip.setOnClickListener(v1 -> {
                    TextEditorActivity.filePath = null;
                    alertDialog1.dismiss();
                });

                btn_replace.setOnClickListener(v12 -> {
                    //file is replaced
                    activity.save(true, file.toString());
                    alertDialog1.dismiss();
                });
                alertDialog1.show();

            }else{
                //file is newly saved
                activity.save(false, file.toString());

                ImageHelper imageHelper = new ImageHelper(getActivity());
                Bitmap bitmap = imageHelper.getBitmapFromUri(OCRHelper.imageUriResultCrop);
                if(bitmap != null){
                    boolean isSave = FileHelper.saveImage(getActivity(), file.toString(), bitmap);
                    if(isSave){
                        OCRHelper.imageUriResultCrop = null;
                        TextEditorActivity.image = bitmap;
                    }
                }

            }

            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);

        return alertDialog;
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

        // bread crumbs
        currentPathView.setText(fileList.getPathOriginRoot().replace(Route.ROOT, "Home"));

        fileList.reloadListView(FileList.currentDirPath);

        if(items == null){
            FileList.StopDirAccess();
            backBtn.setVisibility(View.GONE);
        }

        if(targetParent.equals(FileList.SYMLINK)){
            FileList.StopDirAccess();
            backBtn.setVisibility(View.GONE);
        }
    }

    public boolean isFileNameValid(String fileName){
        if(fileName.equals("")){
            Toast.makeText(getActivity(), "Name is required!", Toast.LENGTH_SHORT).show();
            return false;
        }else if(!fileName.contains(".")){
            Toast.makeText(getActivity(), "File extension is required!", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            String extension = FileHelper.getExtension(fileName); //get file extension
            String name = FileHelper.removeExtension(fileName);
            List<String> allowedExtensions = new ArrayList<>();
            allowedExtensions.add("doc");
            allowedExtensions.add("docx");
            allowedExtensions.add("txt");
            allowedExtensions.add("html");
            allowedExtensions.add("xhtml");

            if(name.equals("")){
                Toast.makeText(getActivity(), "Name is required!", Toast.LENGTH_SHORT).show();
                return false;
            }else if(!Validator.isNameValid(name) || '.' != fileName.charAt(fileName.lastIndexOf("."))){
                Toast.makeText(getActivity(), "Invalid file name!", Toast.LENGTH_SHORT).show();
                return false;
            }else if(extension.isEmpty()){
                Toast.makeText(getActivity(), "File extension is required!", Toast.LENGTH_SHORT).show();
                return false;
            }else if(!allowedExtensions.contains(extension)){
                Toast.makeText(getActivity(), "Invalid file extension!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }
}


