package com.example.androidtrlts.Utils;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    public Task<String> createFile(java.io.File file) {
        return Tasks.call(mExecutor, () -> {
            //file
            String fileName = file.toString().substring(file.toString().lastIndexOf("/")+1);
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName(fileName);

            FileContent mediaContent = new FileContent("text/plain", file);
            File googleFile = mDriveService.files().create(metadata,mediaContent).execute();

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<String> createFile(java.io.File file, String folderId) {
        return Tasks.call(mExecutor, () -> {
            if (folderId.isEmpty()) {
                throw new IOException("Null result when requesting folder ID.");
            }
            //file
            String fileName = file.toString().substring(file.toString().lastIndexOf("/")+1);
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName(fileName)
                    .setParents(Collections.singletonList(folderId));

            FileContent mediaContent = new FileContent("text/plain", file);
            File googleFile = mDriveService.files().create(metadata,mediaContent)
                    .setFields("id, parents")
                    .execute();

            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<String> createFolder(String name) {
        return Tasks.call(mExecutor, () -> {
            String mimeType = "application/vnd.google-apps.folder";
            //check if folder already existed
            //if true then return id otherwise create a new folder
            String folderId = "";
            folderId = searchFileId(name, mimeType);
            if(!folderId.isEmpty()){
                return folderId;
            }

            File folder = new File();
            folder.setName(name);
            folder.setMimeType(mimeType);
            File googleFolder = mDriveService.files().create(folder).setFields("id").execute();

            if (googleFolder == null) {
                throw new IOException("Null result when requesting folder creation.");
            }

            return googleFolder.getId();
        });
    }

    public String searchFileId(String name, String fileType){
        String pageToken = null;
        do{
            try {
                FileList result = null;

                result = mDriveService.files().list()
                        .setQ("mimeType='"+fileType+"' and name ='"+name+"'")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id,name)")
                        .setPageToken(pageToken)
                        .execute();

                if(result.size() < 1){
                    return "";
                }

                for(File file:result.getFiles()){
                    if(file.getName().trim().equalsIgnoreCase(name)){
                        return file.getId();
                    }
                }

                pageToken = result.getNextPageToken();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }while (pageToken != null);

        return "";
    }

}
