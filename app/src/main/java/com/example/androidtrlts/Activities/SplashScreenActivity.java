package com.example.androidtrlts.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androidtrlts.R;


public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        TextView title = findViewById(R.id.textView);
        String text = "<font face='sans-serif-light' color=#76b797>ANDROID</font><font face='sans-serif-black' color=#ffffff>TRLTS</font>";
        title.setText(Html.fromHtml(text));

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}
