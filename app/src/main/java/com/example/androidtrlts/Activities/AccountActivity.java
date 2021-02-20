package com.example.androidtrlts.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.GoogleLib;
import com.example.androidtrlts.Utils.Task;
import com.example.androidtrlts.Utils.Util;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private GoogleLib google;
    private FirebaseUser user;
    private CircleImageView image;
    private TextView name;
    private TextView email;
    private Button status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        google = new GoogleLib(AccountActivity.this);

        image = findViewById(R.id.profile);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        status = findViewById(R.id.status);

        user = google.getUser();

        if(user != null){
            Picasso.get().load(user.getPhotoUrl())
                    .placeholder(R.drawable.man)
                    .into(image);
            name.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            name.setText(user.getDisplayName());
            email.setText(user.getEmail());
            status.setText("Sign out");
        }else{
            image.setImageResource(R.drawable.man);
            name.setVisibility(View.INVISIBLE);
            email.setVisibility(View.INVISIBLE);
            status.setText("Sign in");
        }

        status.setOnClickListener(v -> {
            if(user != null){
                google.signOut();
                image.setImageResource(R.drawable.man);
                name.setVisibility(View.INVISIBLE);
                email.setVisibility(View.INVISIBLE);
                status.setText("Sign in");
                user = null;
                View  view = findViewById(R.id.account);
                Util.showSnackBar(view, "Signed out", getResources().getColor(R.color.success));
            }else{
                google.requestUserSignIn();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        View  view = findViewById(R.id.account);
        if(resultCode == RESULT_OK){
           if (requestCode == GoogleLib.REQUEST_CODE_SIGN_IN) {
                google.handleSignInIntent(data, new Task<Void, String>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Util.showSnackBar(view, "Sign in successful", getResources().getColor(R.color.success));
                        user = google.getUser();
                        if(user != null){
                            Picasso.get().load(user.getPhotoUrl())
                                    .placeholder(R.drawable.man)
                                    .into(image);
                            name.setVisibility(View.VISIBLE);
                            email.setVisibility(View.VISIBLE);
                            name.setText(user.getDisplayName());
                            email.setText(user.getEmail());
                            status.setText("Sign out");
                        }else{
                            image.setImageResource(R.drawable.man);
                            name.setVisibility(View.INVISIBLE);
                            email.setVisibility(View.INVISIBLE);
                            status.setText("Sign in");
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Util.showSnackBar(view, error, getResources().getColor(R.color.error));
                    }
                });
            }
        }

    }
}