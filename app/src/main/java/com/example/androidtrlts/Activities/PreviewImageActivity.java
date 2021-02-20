package com.example.androidtrlts.Activities;


import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.androidtrlts.Helpers.OCRHelper;
import com.example.androidtrlts.R;

public class PreviewImageActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        toolbar = findViewById(R.id.toolbar_preview_image);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setTitle("Preview Image");

        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);
        imageView.setVisibility(View.VISIBLE);
        Uri uri = OCRHelper.imageUriResultCrop;
        if(OCRHelper.imageUriResultCrop != null){
            imageView.setImage(ImageSource.uri(OCRHelper.imageUriResultCrop));
        }else{
            imageView.setImage(ImageSource.bitmap(TextEditorActivity.image));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
