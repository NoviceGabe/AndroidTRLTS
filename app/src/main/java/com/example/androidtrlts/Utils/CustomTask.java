package com.example.androidtrlts.Utils;


import android.app.ProgressDialog;
import android.os.AsyncTask;

public class CustomTask extends AsyncTask<Void, Void, Void> {
    private ProgressDialog progressDialog;

    public interface TaskListener{
        public void onExecute();
        public void onDone();
    }

    private final TaskListener taskListener;

    public CustomTask(ProgressDialog progressDialog, TaskListener taskListener){
        this.progressDialog = progressDialog;
        this.taskListener = taskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(this.taskListener != null){
            this.taskListener.onExecute();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressDialog.dismiss();
        if(this.taskListener != null){
            this.taskListener.onDone();
        }
    }
}
