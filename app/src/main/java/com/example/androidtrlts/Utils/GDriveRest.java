package com.example.androidtrlts.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.view.View;
import androidx.annotation.NonNull;

import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.Collections;

public class GDriveRest {
    public static final int REQUEST_CODE_SIGN_IN = 100;

    private Activity activity;
    private DriveServiceHelper mDriveServiceHelper;
    private GoogleSignInAccount account;
    private GoogleSignInClient client;
    private GoogleSignInOptions signInOptions;

    public GDriveRest(Activity activity){
        this.activity = activity;
        account = GoogleSignIn.getLastSignedInAccount(activity.getApplicationContext());
        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        client = GoogleSignIn.getClient(this.activity, signInOptions);

    }

    public void requestUserSignIn(){
        Intent signInIntent = client.getSignInIntent();
        activity.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }


    public void handleSignInIntent(Intent result, Service service) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleSignInAccount -> {
                    mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService());
                    service.callback();
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Unable to sign in.", Toast.LENGTH_SHORT).show());
    }


    public void uploadFile(File file){
        View view = activity.findViewById(R.id.text_edit_layout);

        if(mDriveServiceHelper == null){
            mDriveServiceHelper = new DriveServiceHelper(getGoogleDriveService());
            if(mDriveServiceHelper == null){
                Util.showSnackBar(view, "Unable to get Google Drive Service!", activity.getResources().getColor(R.color.error));
                return;
            }

        }

        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Uploading to Google Drive");
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String folderName = "AndroidTRLTS";

        mDriveServiceHelper.createFolder(folderName).addOnSuccessListener(folderId ->
                mDriveServiceHelper.createFile(file,  folderId).addOnSuccessListener(s -> {
                    progressDialog.dismiss();
                    Util.showSnackBar(view, "file uploaded successfully", activity.getResources().getColor(R.color.success));
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Util.showSnackBar(view, "failed to upload file", activity.getResources().getColor(R.color.error));
                })).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Util.showSnackBar(view, "failed to create folder", activity.getResources().getColor(R.color.error));
        });
    }

    public void signOut(Service service){

        client.signOut().addOnCompleteListener(activity, task -> {
            Toast.makeText(activity, "Sign out", Toast.LENGTH_SHORT).show();
            service.callback();
        });
    }

    public void revokeAccess(Service service){
        client.revokeAccess().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(activity, "Account disconnected", Toast.LENGTH_SHORT).show();
                service.callback();
            }
        });
    }

    public boolean isSignedIn(){
        return GoogleSignIn.getLastSignedInAccount(activity) != null;
    }

    public com.google.api.services.drive.Drive getGoogleDriveService(){
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        activity, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("AndroidTRLTS")
                        .build();

        return googleDriveService;
    }
    public GoogleSignInAccount getAccount(){
        return account;
    }

    public GoogleSignInClient getClient(){
        return client;
    }

}
