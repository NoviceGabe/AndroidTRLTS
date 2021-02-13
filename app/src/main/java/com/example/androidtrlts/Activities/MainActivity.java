package com.example.androidtrlts.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtrlts.Adapters.SortAdapter;
import com.example.androidtrlts.Fragments.BrowseFragment;
import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.Helpers.ImageHelper;
import com.example.androidtrlts.Helpers.InputMethodHelper;
import com.example.androidtrlts.Helpers.OCRHelper;
import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.CustomTask;
import com.example.androidtrlts.Utils.FileList;
import com.example.androidtrlts.Utils.IFetchContent;
import com.example.androidtrlts.Utils.Route;
import com.example.androidtrlts.Utils.Util;
import com.example.androidtrlts.Utils.Validator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.androidtrlts.Utils.Util.REQUEST_IMAGE_CAPTURE;
import static com.example.androidtrlts.Utils.Util.REQUEST_IMAGE_SELECT;

public class MainActivity extends AppCompatActivity{
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private FloatingActionButton fab_btn, fab_cap, fab_insert_photo, fab_url;
    private CoordinatorLayout fabCon;
    private boolean isOpen = false;
    private Animation fabOpen, fabClose, fabRClockwise, fabRAntiClockwise;

    private InputMethodHelper inputMethodHelper;
    private SessionHelper sessionHelper;

    private static final int FRAGMENT_STATE_REPLACE = 1;
    private static final int FRAGMENT_STATE_ADD = 2;

    public static Util.Property property = Util.Property.NAME;
    public static Util.Order order = Util.Order.ASC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,
                drawerLayout, toolbar, R.string.open,R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        fab_btn = findViewById(R.id.fab_btn);
        fab_cap = findViewById(R.id.capture);
        fab_insert_photo = findViewById(R.id.insert_photo);
        fab_url = findViewById(R.id.url);
        fabCon = findViewById(R.id.fab_container);

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fabRClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_clockwise);
        fabRAntiClockwise = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anticlockwise);

        fab_btn.setOnClickListener(v -> {
            if(isOpen){
                fab_cap.startAnimation(fabClose);
                fab_insert_photo.startAnimation(fabClose);
                fab_url.startAnimation(fabClose);
                fab_btn.startAnimation(fabRClockwise);

                fab_cap.setClickable(false);
                fab_insert_photo.setClickable(false);

                isOpen = false;
            }else{
                fab_cap.startAnimation(fabOpen);
                fab_insert_photo.startAnimation(fabOpen);
                fab_url.startAnimation(fabOpen);
                fab_btn.startAnimation(fabRAntiClockwise);

                fab_cap.setClickable(true);
                fab_insert_photo.setClickable(true);
                fab_url.setClickable(true);

                fab_cap.setVisibility(View.VISIBLE);
                fab_insert_photo.setVisibility(View.VISIBLE);
                fab_url.setVisibility(View.VISIBLE);
                isOpen = true;
            }
        });

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(R.id.drawer)).getChildAt(0);
        inputMethodHelper = new InputMethodHelper(this, viewGroup);

        fab_cap.setOnClickListener(v -> {
            inputMethodHelper.captureImage();
        });

        fab_insert_photo.setOnClickListener(v -> {
            inputMethodHelper.selectImage();
        });

        fab_url.setOnClickListener(v -> {
            alertDialogInput("URL", R.layout.input_url, (IFetchContent<String>) link -> {
                if(link.isEmpty()){
                    Toast.makeText(MainActivity.this, "URL is required!", Toast.LENGTH_SHORT).show();
                }else{
                    inputMethodHelper.insertUrl(link, (IFetchContent<Uri>) uri -> {
                        ImageHelper imageHelper = new ImageHelper(MainActivity.this);
                        imageHelper.startCrop(uri);
                    });
                }
            });

        });

        sessionHelper = new SessionHelper(this);
        init();
        loadFragment(FRAGMENT_STATE_REPLACE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFragment(FRAGMENT_STATE_REPLACE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu_items, menu);

        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // when input text change
               // load fragment - filter listview
                return false;
            }
        });

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // when edit text collapse
                // clear the input to reload the fragment
                searchView.setQuery("", false);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.search_bar) {
        } else if (itemId == R.id.create_folder) {
            alertDialogInput("Create Folder", R.layout.text_input, (IFetchContent<String>) content -> {
                if(content.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a name", Toast.LENGTH_SHORT).show();
                }else if(!Validator.isNameValid(content)){
                    Toast.makeText(MainActivity.this, "Invalid folder name!", Toast.LENGTH_SHORT).show();
                }else{
                    File dir = new File(FileList.currentDirPath+ content); // file directory
                    if(dir.isDirectory() && dir.exists()){
                        Toast.makeText(MainActivity.this, "folder already existed!", Toast.LENGTH_SHORT).show();
                    }else{
                        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Processing..");

                        CustomTask task = new CustomTask(progressDialog, new CustomTask.TaskListener() {
                            @Override
                            public void onExecute() {
                                View  view = findViewById(R.id.drawer);

                                if(dir.mkdir()){
                                    Util.showSnackBar(view, "\"" + content + "\" has been created", getResources().getColor(R.color.success));
                                }else{
                                    Util.showSnackBar(view, "Unable to create a folder", getResources().getColor(R.color.error));
                                }
                            }

                            @Override
                            public void onDone() {
                                loadFragment(FRAGMENT_STATE_REPLACE);
                            }
                        });

                        task.execute();
                    }
                }
            });
        } else if (itemId == R.id.sort) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View row = getLayoutInflater().inflate(R.layout.sort,null);
            ListView listView = row.findViewById(R.id.listView);

            Button asc = row.findViewById(R.id.asc);
            Button desc = row.findViewById(R.id.desc);

            if(order == Util.Order.ASC){
                asc.getBackground().setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.MULTIPLY);
                desc.getBackground().setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.MULTIPLY);
            }else{
                desc.getBackground().setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.MULTIPLY);
                asc.getBackground().setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.MULTIPLY);
            }

            asc.setOnClickListener(v -> {
                order = Util.Order.ASC;
                loadFragment(FRAGMENT_STATE_REPLACE);
                asc.getBackground().setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.MULTIPLY);
                desc.getBackground().setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.MULTIPLY);
            });

            desc.setOnClickListener(v -> {
                order = Util.Order.DESC;
                loadFragment(FRAGMENT_STATE_REPLACE);
                desc.getBackground().setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.MULTIPLY);
                asc.getBackground().setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.MULTIPLY);
            });

            List<String> items = new ArrayList<>();
            items.add("Name");
            items.add("Date");

            SortAdapter sortAdapter = new SortAdapter(MainActivity.this, items);
            listView.setAdapter(sortAdapter);

            builder.setCancelable(true);
            builder.setView(row);

            TextView textView = new TextView(MainActivity.this);
            textView.setText("Sort");
            textView.setPadding(20, 30, 20, 30);
            textView.setTextSize(20f);
            builder.setCustomTitle(textView);


            final AlertDialog dialog = builder.create();

            dialog.show();

        } else if (itemId == R.id.bin) {
        } else if (itemId == R.id.settings) {
        } else if (itemId == R.id.exit) {
            exit();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        View  view = findViewById(R.id.drawer);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_IMAGE_SELECT){
                if(data != null){
                    Uri imgUri = data.getData();
                    if(imgUri != null){
                        cropImage(imgUri);
                    }
                }
            }else if(requestCode == REQUEST_IMAGE_CAPTURE){
                ImageHelper imageHelper = new ImageHelper(this);
                Bitmap bitmap = (Bitmap) data.getExtras().get("data"); // set bitmap
                Uri uri = imageHelper.getUriFromBitmap(bitmap);
                cropImage(uri);
            }else if (requestCode == UCrop.REQUEST_CROP) {
                OCRHelper.imageUriResultCrop = UCrop.getOutput(data); //get cropped image URI
                if (OCRHelper.imageUriResultCrop == null) {
                    Util.showSnackBar(view, "cannot crop image", getResources().getColor(R.color.error));
                    return;
                }

                OCRHelper.runOnCloudTextRecognition(this);
            }
        }

    }

    private void cropImage(Uri uri){
        View  view = findViewById(R.id.drawer);
        ImageHelper imageHelper = new ImageHelper(this);

        if(uri == null){
            Util.showSnackBar(view, "image URI doesn't exist", getResources().getColor(R.color.error));
            return;
        }
        try {
            imageHelper.startCrop(uri); //start crop
        }catch (Exception exception){
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void init(){
        try {
            File parent = new File(Route.parent);
            FileHelper.createDirectory(parent, Route.ROOT);
        } catch (Exception exception) {
            Log.d("storage", exception.getMessage());
        }
    }

    public void loadFragment(final int FRAGMENT_STATE){
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        BrowseFragment fragment = new BrowseFragment();

        switch(FRAGMENT_STATE){
            case FRAGMENT_STATE_ADD:
                fragmentTransaction.add(R.id.fragment_container, fragment);
                break;
            case FRAGMENT_STATE_REPLACE:
                fragmentTransaction.replace(R.id.fragment_container, fragment);
        }

        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        boolean isConfirmOnExit = sessionHelper.getBoolean("confirm_exit", false);
        if(Util.removeTrailingChar(FileList.currentDirPath, "/").equals(Route.getFullPath())){
            if(isConfirmOnExit){
                finish();
            }else{
                exit();
            }
        }else{
            FileList.action = Util.Action.CLOSE;
            loadFragment(FRAGMENT_STATE_REPLACE);
        }
    }

    public void alertDialogInput(String title, int layoutId, IFetchContent fetch){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        TextView tvTitle = new TextView(this);
        tvTitle.setPadding(20, 30, 20, 30);
        tvTitle.setTextSize(20f);
        tvTitle.setText(title);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        builder.setCustomTitle(tvTitle);

        View mView = getLayoutInflater().inflate(layoutId, null);
        final EditText input = (EditText) mView.findViewById(R.id.input);
        input.setOnFocusChangeListener((v, hasFocus) -> input.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }));
        input.requestFocus();

        builder.setView(mView);

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            fetch.onFetch(input.getText().toString());
            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void exit(){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton("NO", null) // dismisses by default
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .create()
                .show();
    }
}