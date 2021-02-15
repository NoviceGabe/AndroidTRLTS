package com.example.androidtrlts.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.androidtrlts.Activities.TextEditorActivity;
import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DB {
    private FirebaseFirestore db;
    private Activity activity;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;

    public DB(Activity activity) {
        this.db = FirebaseFirestore.getInstance();
        this.activity = activity;
        storage = FirebaseStorage.getInstance();
    }

    public void addUserData(Map map, com.example.androidtrlts.Utils.Task task){
        if(map == null || map.get("uid") == null){
            Toast.makeText(activity, "Empty data", Toast.LENGTH_SHORT).show();
            return;
        }

        map.put("timestamp", FieldValue.serverTimestamp());

        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document((String) map.get("uid"))
                .set(map).addOnSuccessListener(aVoid -> {
            task.onSuccess(aVoid);
        }).addOnFailureListener(e -> {
            task.onError(e.getMessage());
        });
    }

    public void getUser(int uid, com.example.androidtrlts.Utils.Task task){
        DocumentReference ref = db.document("users/"+uid);
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    task.onSuccess(documentSnapshot.getData());
                } else {
                    task.onError("No user");
                }
            }
        }).addOnFailureListener(e -> {
            task.onError(e.getMessage());
        });
    }

    public void updateFile(com.example.androidtrlts.Model.File file, Task task){
        Map<String, Object> data = new HashMap<>();
        data.put("filename", file.getFilename());
        data.put("lastUpdated", file.getLastUpdated());

        db.collection("files").document(file.getId()).update(data)
                .addOnSuccessListener(aVoid -> {
                   task.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    task.onSuccess(e.getMessage());
                });
    }

    public void getFile(String uid, com.example.androidtrlts.Model.File file, Task task){
        db.collection("files")
                .whereEqualTo("uid", uid)
                .whereEqualTo("dir", file.getDir())
                .whereEqualTo("filename", file.getFilename())
                .get()
                .addOnSuccessListener(documentSnapshots -> {

                    if (!documentSnapshots.isEmpty()) {
                        List<com.example.androidtrlts.Model.File> types = documentSnapshots
                                .toObjects(com.example.androidtrlts.Model.File.class);
                       task.onSuccess(types);
                    }
                    task.onError("404");
                }).addOnFailureListener(e -> {
                task.onError(e.getMessage());
            });
    }

    public void addFile(Map map, Task task){
        if(map == null || map.get("uid") == null){
            Toast.makeText(activity, "Empty data", Toast.LENGTH_SHORT).show();
            return;
        }

        map.put("timestamp", FieldValue.serverTimestamp());

        db.collection("files")
                .document((String) map.get("id")).set(map).addOnSuccessListener(aVoid -> {
            task.onSuccess(aVoid);
        }).addOnFailureListener(e -> {
            task.onError(e.getMessage());
        });
    }

    public DocumentReference createDocumentRef(String collection){
        return db.collection(collection).document();
    }

    public void uploadFileToStorage(String uid, String id,  String filePath, Task task,
                                    ProgressDialog dialog){
        try {
            File file = new File(filePath);
            Uri upload = Uri.fromFile(file);
            String name = FileHelper.getName(file);
            String extension = FileHelper.getExtension(file.toString());

            if(dialog != null){
                dialog.show();
            }

            mStorageRef = storage.getReference().child(uid+"/backup/"+id+"/"+name+"."+extension);

            mStorageRef.putFile(upload)
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                       if(dialog != null){
                           dialog.dismiss();
                       }
                        task.onSuccess(null);
                    })
                    .addOnFailureListener(exception -> {
                        if(dialog != null){
                            dialog.dismiss();
                        }
                        task.onError(exception.getMessage());
                    });

        }catch (Exception e){
            if(dialog != null){
                dialog.dismiss();
            }
            task.onError(e.getMessage());
        }

    }
}
