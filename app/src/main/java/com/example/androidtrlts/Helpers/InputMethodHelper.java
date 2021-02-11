package com.example.androidtrlts.Helpers;

import android.Manifest;
import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.CustomTask;
import com.example.androidtrlts.Utils.IFetchContent;
import com.example.androidtrlts.Utils.InputMethod;
import com.example.androidtrlts.Utils.Util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.example.androidtrlts.Utils.Util.REQUEST_IMAGE_SELECT;
import static com.example.androidtrlts.Utils.Util.READ_EXTERNAL_STORAGE;
import static com.example.androidtrlts.Utils.Util.REQUEST_IMAGE_CAPTURE;

public class InputMethodHelper implements InputMethod {
    private Activity activity;
    private  PermissionHelper permissionHelper;
    private View view;
    private AlertDialog alertDialog;
    private Bitmap bitmap;

    public InputMethodHelper(Activity activity, View view){
        this.activity = activity;
        permissionHelper = new PermissionHelper(this.activity);
        this.view = view;
    }

    public void setAlertDialog(AlertDialog alertDialog){
        this.alertDialog = alertDialog;
    }

    @Override
    public void selectImage() {
        permissionHelper.checkPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE, new PermissionHelper.PermissionAskListener() {
            @Override
            public void onNeedPermission() {
                ActivityCompat.requestPermissions(activity,  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                permissionHelper.showRational("Permission Denied",
                        "Without this permission this app is unable to access storage. Are you sure you want to deny this permission?",
                        () -> ActivityCompat.requestPermissions(activity,  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE));
            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskingAgain() {
                permissionHelper.showDialogForSettings("Permission Denied", "Now you must allow storage access from settings.");
            }

            @Override
            public void onPermissionGranted() {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activity.startActivityForResult(intent, REQUEST_IMAGE_SELECT);
            }
        });
    }

    @Override
    public void captureImage() {
        permissionHelper.checkPermission(activity, Manifest.permission.CAMERA, new PermissionHelper.PermissionAskListener() {
            @Override
            public void onNeedPermission() {
                ActivityCompat.requestPermissions(activity,  new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                permissionHelper.showRational("Permission Denied",
                        "Without this permission this app is unable to open camera to take your photo. Are you sure you want to deny this permission?",
                        () -> ActivityCompat.requestPermissions(activity,  new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE));
            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskingAgain() {
                permissionHelper.showDialogForSettings("Permission Denied", "Now you must allow camera access from settings.");
            }

            @Override
            public void onPermissionGranted() {
                ImageHelper imageHelper = new ImageHelper(activity);
                imageHelper.dispatchTakePictureIntent();
            }
        });

    }

    @Override
    public void insertUrl(String link, IFetchContent fetch) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Processing..");

        CustomTask task = new CustomTask(progressDialog, new CustomTask.TaskListener() {
            @Override
            public void onExecute() {
                try {
                    URL url = new URL(link);
                    InputStream input = url.openConnection().getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(input, 1024 * 8);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while((len = bis.read(buffer)) != -1){
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    bis.close();

                    byte[] data = out.toByteArray();
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                } catch (IOException e) {
                    Util.showSnackBar(view, e.getMessage(), activity.getResources().getColor(R.color.error));
                }
            }

            @Override
            public void onDone() {
                ImageHelper imageHelper = new ImageHelper(activity);

                if(bitmap != null){
                    Uri uri = imageHelper.getUriFromBitmap(bitmap);
                    fetch.onFetch(uri);
                    bitmap = null;
                }else{
                    Util.showSnackBar(view, "Unable to load image from " + link, activity.getResources().getColor(R.color.error));
                }

            }
        });

        task.execute();
    }
}
