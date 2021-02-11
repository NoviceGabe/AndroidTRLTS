package com.example.androidtrlts.Helpers;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.androidtrlts.Utils.Service;

public class PermissionHelper {
    private Context context;
    private SessionHelper sessionManager;

    public PermissionHelper(Context context){
        this.context = context;
        sessionManager = new SessionHelper(context);
    }

    public boolean isFirstTimeAsking(String key){
        return sessionManager.getBoolean(key, true);
    }

    public void firstTimeAsking(String key, boolean isFirstTime){
        sessionManager.putBoolean(key, isFirstTime);
    }

    public boolean shouldAskPermission(){
        return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M);
    }

    private boolean shouldAskPermission(Context context, String permission){
        if(shouldAskPermission()){
            int permissionResult = ActivityCompat.checkSelfPermission(context, permission);
            if(permissionResult != PackageManager.PERMISSION_GRANTED){
                return true;
            }
        }
        return false;
    }

    public void checkPermission(Context context, String permission, PermissionAskListener listener){
        if(shouldAskPermission(context, permission)){
            if(ActivityCompat.shouldShowRequestPermissionRationale((AppCompatActivity)context, permission)){
                listener.onPermissionPreviouslyDenied();
            }else{
                if(isFirstTimeAsking(permission)){
                    firstTimeAsking(permission, false);
                    listener.onNeedPermission();
                }else{
                    listener.onPermissionPreviouslyDeniedWithNeverAskingAgain();
                }
            }
        }else{
            listener.onPermissionGranted();
        }
    }


    private void gotoSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.parse("package:" + context.getPackageName());
        intent.setData(uri);
        context.startActivity(intent);
    }

    public void showRational(String title, String message, Service service){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("I'm sure", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Retry", (dialog, which) -> {
                    service.callback();
                    dialog.dismiss();
                }).show();
    }

    public void showDialogForSettings(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Not now", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Settings", (dialog, which) -> {
                    gotoSettings();
                    dialog.dismiss();
                }).show();
    }

    public interface PermissionAskListener{
        void onNeedPermission();
        void onPermissionPreviouslyDenied();
        void onPermissionPreviouslyDeniedWithNeverAskingAgain();
        void onPermissionGranted();
    }

}