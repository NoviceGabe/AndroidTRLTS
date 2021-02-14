package com.example.androidtrlts.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidtrlts.Adapters.CustomDialogBaseAdapter;
import com.example.androidtrlts.Fragments.DialogFragment;
import com.example.androidtrlts.Fragments.TTSDialogFragment;
import com.example.androidtrlts.Helpers.FileHelper;
import com.example.androidtrlts.Helpers.PermissionHelper;
import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.DialogItem;
import com.example.androidtrlts.Utils.FileList;
import com.example.androidtrlts.Utils.GoogleLib;
import com.example.androidtrlts.Utils.IFetchContent;
import com.example.androidtrlts.Utils.Language;
import com.example.androidtrlts.Utils.Route;
import com.example.androidtrlts.Utils.Util;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import net.dankito.richtexteditor.android.RichTextEditor;
import net.dankito.richtexteditor.android.toolbar.AllCommandsEditorToolbar;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.example.androidtrlts.Utils.Util.REQUEST_AUDIO;
import static com.example.androidtrlts.Utils.Util.WRITE_EXTERNAL_STORAGE;

public class TextEditorActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RichTextEditor editor;
    private AllCommandsEditorToolbar editorToolbar;
    private MenuItem saveItem;
    private int menuIconColor;

    private String text = "";
    private String title = "";
    public static String filePath;
    public static Bitmap image;
    private boolean enableSave = true;
    private boolean isFileSave = false;
    private boolean close = false;
    private boolean init = false;

    private PermissionHelper permissionHelper;
    private FirebaseStorage storage;
    private StorageReference mStorageRef;

    private GoogleLib google;

    private boolean pref_auto_save;
    private String pref_font_family;

    private SessionHelper sessionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        permissionHelper = new PermissionHelper(this);
        storage = FirebaseStorage.getInstance();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setTitle("");
        editorToolbar = findViewById(R.id.editorToolbar);
        editor = findViewById(R.id.editor);
        editorToolbar.setEditor(editor);

        Bundle bundle = getIntent().getExtras();
        String extractedText = "";

        if(bundle!=null){
            if(bundle.containsKey("extractedText")){
                extractedText = bundle.getString("extractedText");
                if(extractedText == null){
                    Toast.makeText(this, "Error: Unable to retrieve text", Toast.LENGTH_SHORT).show();
                }else if(extractedText.isEmpty()){
                    Toast.makeText(this, "No text extracted", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Unknown error occurred!", Toast.LENGTH_SHORT).show();
            }

            if(bundle.containsKey("title")){
                title = bundle.getString("title");
            }

            if(filePath != null && !filePath.isEmpty()){
                // if the file is coming from the main activity
                // get the file path from the selected file on the main activity
                disableSave(); // the save button must be disabled from the startup because no changes has been done yet
            }

        }else{
            Toast.makeText(this, "Error: Null", Toast.LENGTH_SHORT).show();
            return;
        }
        initEditor(extractedText);

        google = new GoogleLib(TextEditorActivity.this);
        sessionHelper = new SessionHelper(TextEditorActivity.this);
        sessionHelper.initDefaultSharedPreferences();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // if save button is enable, save file on activity change
        if(enableSave){
            pref_auto_save = sessionHelper.getSessionBoolean("pref_auto_save");
            onAutoSave(pref_auto_save);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);

        saveItem = menu.getItem(4); // save icon
        String path = filePath;
        boolean save = enableSave;
        // disabled save button
        if((filePath != null && !filePath.isEmpty()) && saveItem != null){
            Drawable drawable = saveItem.getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.parseColor("#808080"), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int color = Color.parseColor("#808080"); // disabled

        if(enableSave){
            color = Color.parseColor("#ffffff"); // enabled
        }

        if(saveItem != null) {
            Drawable drawable = saveItem.getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent();

        switch(item.getItemId()){
            case R.id.image: // preview image
                intent = new Intent(TextEditorActivity.this, PreviewImageActivity.class);
                startActivity(intent);
                return true;

            case R.id.copy: //copy the current text to the clipboard

                editor.getCurrentHtmlAsync(str -> {
                    String text = Util.html2text(str).trim();
                    Util.copyToClipBoard(this,text);
                });
                return true;
            case R.id.trans_lang:

                List lang = TranslateLanguage.getAllLanguages();
                List<String> languages = new ArrayList<>();
                for (Object s : lang) {
                    Language l = new Language(s.toString());
                    languages.add(l.getDisplayName());
                }
                Collections.sort(languages); // sort to ascending order
                String pref_source_lang = "1";

                editor.getCurrentHtmlAsync(s -> {
                    String ftext = Util.html2text(s).trim();
                    IndetifyLanguage(ftext, (IFetchContent<String>) langCode -> {
                        final Language language = new Language(langCode);

                        AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.dialog_lang_menu,null);

                        final Spinner sourceLangMenu = view.findViewById(R.id.source_lang_menu);

                        if(pref_source_lang.equals("1")){
                            String detected = "Language detected: " +
                                    language.getDisplayName().substring(0,1).toUpperCase() + language.getDisplayName().substring(1).toLowerCase();

                            TextView lang_detected = view.findViewById(R.id.lang_detected);
                            lang_detected.setVisibility(View.VISIBLE);
                            lang_detected.setText(detected);
                            lang_detected.setTextColor(Color.BLACK);
                        }else{
                            sourceLangMenu.setVisibility(View.VISIBLE);
                            TextView sourceLangTitle = view.findViewById(R.id.source_lang);
                            sourceLangTitle.setVisibility(View.VISIBLE);
                            ArrayAdapter<String> sourceLangAdapter = new ArrayAdapter<String>(TextEditorActivity.this,
                                    android.R.layout.simple_spinner_item, languages);

                            sourceLangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sourceLangMenu.setAdapter(sourceLangAdapter);
                        }

                        final Spinner targetLangMenu = view.findViewById(R.id.target_lang_menu);
                        TextView targetLangTitle = view.findViewById(R.id.target_lang);
                        ArrayAdapter<String> targetLangAdapter = new ArrayAdapter<>(TextEditorActivity.this,
                                android.R.layout.simple_spinner_item, languages);

                        targetLangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        targetLangMenu.setAdapter(targetLangAdapter);

                        TextView alertDialogTitle = new TextView(TextEditorActivity.this);
                        alertDialogTitle.setText("Translate");
                        alertDialogTitle.setPadding(20, 30, 20, 10);
                        alertDialogTitle.setTextSize(20f);
                        alertDialogTitle.setTextColor(Color.BLACK);

                        builder.setCustomTitle(alertDialogTitle);
                        builder.setView(view);


                        builder.setPositiveButton("Ok", (dialog, which) -> {
                            String targetItem = targetLangMenu.getSelectedItem().toString();
                            String targetLang = TranslateLanguage.ENGLISH;

                            for (Object t : lang) {
                                Language l = new Language(t.toString());
                                if(l.getDisplayName().equals(targetItem)){
                                    targetLang = l.getCode();
                                    break;
                                }
                            }
                            translateText(ftext, language.getCode(), targetLang);

                        }).setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss()).setView(view);

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        // get screen width and height in pixels
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int displayWidth = displayMetrics.widthPixels;

                        Window window = alertDialog.getWindow();
                        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                        layoutParams.copyFrom(window.getAttributes());

                        // alert dialog width equal to 90% of screen width
                        int dialogWindowWidth = (int) (displayWidth * 0.90f);

                        layoutParams.width = dialogWindowWidth;
                        layoutParams.height = WRAP_CONTENT;
                        window.setAttributes(layoutParams);

                    });
                });

                return true;
            case R.id.audio:
                intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(intent, REQUEST_AUDIO);
                return true;
            case R.id.save:
                if(enableSave){
                    // Firstly checks the filePath value. If the filePath returns null or
                    // doesn't contain any directory path, that means there's no previous saved file.
                    // And the additional isFileSave boolean variable is used to check if the file hasn't
                    // been saved yet, then the file will be saved for the first time using saveAs method
                    //otherwise the file will be overwritten using the save method
                    if((filePath == null || filePath.isEmpty()) && !isFileSave){
                        saveAs();
                    }else{
                        save(true, filePath);
                    }
                }
                return true;

            case R.id.save_as:
                saveAs();
                return true;

            case R.id.export:
                showExportDialog();
                return true;

            case R.id.share:
                showShareDialog();
                return true;

            case R.id.backup:
                showBackupDialog();
                return true;

            case R.id.settings:
                intent = new Intent(TextEditorActivity.this, EditorSettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.close:
                close = true;
                leave();
                return true;
            case android.R.id.home:
                leave();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        leave();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        View  view = findViewById(R.id.text_edit_layout);

        if (resultCode == Activity.RESULT_OK && resultData != null) {
            if(requestCode == Util.SHARE_REQUEST_CODE){
                Util.showSnackBar(view, "Shared",getResources().getColor(R.color.success));
            }else{
                Util.showSnackBar(view, "Unknown error occur!", getResources().getColor(R.color.error));
            }
        }

        if(requestCode == REQUEST_AUDIO){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                //Retrieved the current text
                //and identify the text language
                editor.getCurrentHtmlAsync(str -> {
                    final String text = Util.html2text(str).trim();
                    IndetifyLanguage(text, (IFetchContent<String>) langCode -> {
                        Language language = new Language(langCode);
                        Bundle bundle = new Bundle();
                        bundle.putString("text",text);
                        bundle.putString("code",language.getCode());
                        bundle.putString("lang", language.getDisplayName());
                        TTSDialogFragment fragment = new TTSDialogFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragment.show(fragmentManager, text);
                    });
                });

            }else{
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }


        super.onActivityResult(requestCode, resultCode, resultData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPreferences();
    }


    private void initPreferences(){
        // shared preferences
        boolean pref_keyboard = sessionHelper.getSessionBoolean("pref_keyboard");
        boolean pref_mode = sessionHelper.getSessionBoolean("pref_mode");
        int pref_color_picker = sessionHelper.getSessionInt("pref_color_picker", Color.BLACK);
        int pref_color_picker_background = sessionHelper.getSessionInt("pref_color_picker_background", Color.WHITE);
        String pref_theme = sessionHelper.getSessionString("pref_theme", "-1");
        pref_auto_save = sessionHelper.getSessionBoolean("pref_auto_save");
        pref_font_family = sessionHelper.getSessionString("pref_font_family", "serif");
        editor.setEditorFontSize(sessionHelper.getSessionInt("pref_font_size", 14));
        editor.setEditorFontFamily(pref_font_family);
        editor.setPadding((int)(4 * getResources().getDisplayMetrics().density));

        Window window = null;
        if(Build.VERSION.SDK_INT >= 21){
            window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // material theme
        switch (Integer.valueOf(pref_theme)){
            case 2:// lighter
                editorUIColor(Color.parseColor("#fafafa"), Color.parseColor("#546f7a"),
                        Color.parseColor("#273238"), Color.parseColor("#273238"),
                        Color.parseColor("#546f7a"));
                break;
            case 3:// one light
                editorUIColor(Color.parseColor("#fafafa"), Color.parseColor("#232323"),
                        Color.parseColor("#273238"), Color.parseColor("#273238"),
                        Color.parseColor("#fafafa"));
                break;
            case 4:// github
                editorUIColor(Color.parseColor("#f7f8fa"), Color.parseColor("#5a6169"),
                        Color.parseColor("#17181a"), Color.parseColor("#17181a"),
                        Color.parseColor("#5a6169"));
                break;
            case 5:// oceanic
                editorUIColor(Color.parseColor("#273238"), Color.parseColor("#b0bfc6"),
                        Color.parseColor("#273238"), Color.parseColor("#273238"),
                        Color.parseColor("#b0bfc6"));
                break;
            case 6://monokai pro
                editorUIColor(Color.parseColor("#2c2a2d"), Color.parseColor("#fcfcfa"),
                        Color.parseColor("#273238"), Color.parseColor("#273238"),
                        Color.parseColor("#fcfcfa"));
                break;
            case 7:// ark dark
                editorUIColor(Color.parseColor("#30343f"), Color.parseColor("#d3dae4"),
                        Color.parseColor("#273238"), Color.parseColor("#273238"),
                        Color.parseColor("#d3dae4"));
                break;
            case 8:// dracula
                editorUIColor(Color.parseColor("#282a36"), Color.parseColor("#f7f8f2"),
                        Color.parseColor("#273238"), Color.parseColor("#273238"),
                        Color.parseColor("#f7f8f2"));
                break;
            case 9:// darker
                editorUIColor(Color.parseColor("#212121"), Color.parseColor("#b0bfc6"),
                        Color.parseColor("#273238"), Color.parseColor("#273238"),
                        Color.parseColor("#b0bfc6"));
                break;
            default:

                editorUIColor(pref_color_picker_background, pref_color_picker, getResources().getColor(R.color.colorPrimary),
                        getResources().getColor(R.color.colorPrimary), Color.WHITE);
                if(window != null) {
                        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                        window.setNavigationBarColor(Color.parseColor("#f2f2f2"));
                }


        }
        invalidateOptionsMenu();
        // @if edit mode is disable - hide the bottom toolbar and make the text read only
        // @else edit mode is enable - show the bottom toolbar and make the text read and write
        if(!pref_mode){
            editor.setInputEnabled(false);
            editorToolbar.setVisibility(View.GONE);
        }else{
            // show keyboard and focus text on startup
            if(pref_keyboard){
                editor.focusEditorAndShowKeyboardDelayed();
            }
            editor.setInputEnabled(true);
            editorToolbar.setVisibility(View.VISIBLE);
        }
    }


    private void initEditor(@NotNull String text){
        ProgressDialog progressDialog = new ProgressDialog(TextEditorActivity.this);
        progressDialog.setTitle("Initializing text editor");
        progressDialog.setMessage("Preparing text..");
        progressDialog.show();
        progressDialog.setCancelable(false);

        if(text.length() > 0){
            text = text.replaceAll("\n","<br>"); //format result text to html
        }

        this.text = Util.html2text(text).trim(); // get the string

        editor.setHtml(text);

        // on editor load set the editor default font family
        editor.addEditorLoadedListener(() -> {
            progressDialog.dismiss();
        });

        // detects if changes has been done on the text

        editor.addHtmlChangedListener(s -> {
            runOnUiThread(() -> {
                // retrieves the current text then compares to the previous one
                // if both text don't match that means the text has been modified
                // enables the save button
                String ftext = Util.html2text(s).trim();
                if(!ftext.equals(this.text) && ftext.length() > 1){
                    enableSave();
                }
            });
        });

    }

    public void save(boolean init, String filePath){
        this.init = init;
        permissionHelper.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionHelper.PermissionAskListener() {
            @Override
            public void onNeedPermission() {
                ActivityCompat.requestPermissions(TextEditorActivity.this,  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE);
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                permissionHelper.showRational("Permission Denied",
                        "Without this permission this app is unable to write to storage. Are you sure you want to deny this permission?",
                        () -> ActivityCompat.requestPermissions(TextEditorActivity.this,  new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE));
            }

            @Override
            public void onPermissionPreviouslyDeniedWithNeverAskingAgain() {
                permissionHelper.showDialogForSettings("Permission Denied", "Now you must allow storage access from settings.");
            }

            @Override
            public void onPermissionGranted() {
                editor.getCurrentHtmlAsync(str -> {
                    saveToTextFile(str, filePath);
                });
            }
        });
    }

    private void saveAs(){
        dialogSaveFile();
    }

    private void saveToTextFile(String text, String filePath){
        View view = findViewById(R.id.text_edit_layout);
        try{
            //checks if root (OCR_DEV) directory exists
            //if not then create one
            File parent = new File(Route.parent);
            if(!parent.exists()){
                FileHelper.createDirectory(parent, Route.ROOT);
            }
            File file = new File(filePath);
            FileWriter fw = new FileWriter(file); //create the file
            BufferedWriter bw = new BufferedWriter(fw);
            String ftext;
            final String extension = FileHelper.getExtension(filePath);
            if(extension.equals("html") || extension.equals("xhtml") ||
                    extension.equals("doc") || extension.equals("docx") ||
                    extension.equals("rtf")){
                ftext = text; // let the text as it is
            }else{
                ftext = Util.html2text(text).trim(); // filter the text
            }

            if((TextEditorActivity.filePath != null && !TextEditorActivity.filePath.isEmpty()) &&
                    ftext.equals(this.text)){
                throw new Exception("No changes");
            }else if(ftext.trim().length() == 0){
                throw new Exception("Empty document");
            }

            bw.write(ftext); //write text to file
            bw.close();

            isFileSave = true; //a file has been saved
            disableSave();// disable button

            if(!init){
                Util.showSnackBar(view, "Saved to " + FileList.currentDirPath, getResources().getColor(R.color.success));
            }else{
                Util.showSnackBar(view, "Saved", getResources().getColor(R.color.success));
            }

            this.text = ftext;
            init = false;

        }catch (Exception e){
            isFileSave = false;
            Util.showSnackBar(view, "File not save", getResources().getColor(R.color.error));
            Toast.makeText(TextEditorActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void dialogSaveFile(){
        Bundle bundle = new Bundle();
        bundle.putString("title", title);

        DialogFragment dialogFragment = new DialogFragment();
        dialogFragment.setArguments(bundle);
        dialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.customDialog);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "Save as");
    }

    private void disableSave(){
        enableSave = false;
        invalidateOptionsMenu();
    }

    private void enableSave(){
        if(!enableSave){
            enableSave = true;
            invalidateOptionsMenu();
        }
    }

    private void leave(){
        // check if save button is enable
        boolean test = enableSave;
        if(enableSave){
            AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
            String message = "Text has been modified, discard changes?";
            if (!isFileSave && (filePath !=null && !filePath.isEmpty())) {
                message = "File has not been save yet, discard the result?";
            }

            builder.setMessage(message).setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                            if (close) {
                                startActivity(new Intent(TextEditorActivity.this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                filePath = "";
                            } else {
                                finish();
                                filePath = "";
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return;
        }

        if(close){
            startActivity(new Intent(TextEditorActivity.this, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }else{
            TextEditorActivity.super.onBackPressed();
        }
        filePath = "";

    }

    private void showExportDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
        View row = getLayoutInflater().inflate(R.layout.listview,null);
        ListView listView = row.findViewById(R.id.listView);

        ArrayList<DialogItem> list = new ArrayList<>();
        DialogItem item;
        item = new DialogItem("as PDF(.pdf)","pdf");
        list.add(item);

        CustomDialogBaseAdapter adapter = new CustomDialogBaseAdapter(list, TextEditorActivity.this);
        listView.setAdapter(adapter);

        builder.setCancelable(true);
        builder.setView(row);

        TextView textView = new TextView(TextEditorActivity.this);
        textView.setText("Export");
        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20f);

        builder.setCustomTitle(textView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            View  mview = findViewById(R.id.text_edit_layout);
            switch((int)id){
                case 0:
                    editor.getCurrentHtmlAsync(s -> {
                        String ftext = Util.html2text(s).trim();

                        if(FileHelper.exportTextToPDF(TextEditorActivity.this, ftext, title) != null){
                            Util.showSnackBar(mview, "File successfully exported as PDF", getResources().getColor(R.color.success));
                        }else{
                            Util.showSnackBar(mview, "Unable to export file to PDF", getResources().getColor(R.color.error));
                        }
                    });

                    dialog.dismiss();
                    break;
            }
        });
    }

    private void showShareDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
        View row = getLayoutInflater().inflate(R.layout.listview,null);
        ListView listView = row.findViewById(R.id.listView);

        ArrayList<DialogItem> list = new ArrayList<>();
        DialogItem item;
        item = new DialogItem("as text","share_text");
        list.add(item);
        item = new DialogItem("as PDF(.pdf)","pdf");
        list.add(item);
        item = new DialogItem("Share now","share_any");
        list.add(item);

        CustomDialogBaseAdapter adapter = new CustomDialogBaseAdapter(list, TextEditorActivity.this);
        listView.setAdapter(adapter);

        builder.setCancelable(true);
        builder.setView(row);

        TextView textView = new TextView(TextEditorActivity.this);
        textView.setText("Share");
        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20f);

        builder.setCustomTitle(textView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            switch ((int)id){
                case 0:
                    editor.getCurrentHtmlAsync(s -> {
                        String ftext = Util.html2text(s).trim(); // filter the text
                        shareText(ftext);
                    });
                    break;
                case 1:
                    editor.getCurrentHtmlAsync(s -> {
                        String ftext = Util.html2text(s).trim(); // filter the text
                        FileHelper.shareTextToPDF(TextEditorActivity.this, ftext, title);
                    });
                    break;
                case 2:
                    // if text was modified save file before sharing
                    if(enableSave){
                        onAutoSave(true);
                    }
                    File file = new File(filePath);;
                    FileHelper.shareFile(TextEditorActivity.this, file, "*/*");

            }
            dialog.dismiss();
        });

    }

    private void showBackupDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(TextEditorActivity.this);
        View row = getLayoutInflater().inflate(R.layout.listview,null);
        ListView listView = row.findViewById(R.id.listView);

        ArrayList<DialogItem> list = new ArrayList<>();
        DialogItem item;
        item = new DialogItem("Cloud storage","backup");
        list.add(item);

        CustomDialogBaseAdapter adapter = new CustomDialogBaseAdapter(list, TextEditorActivity.this);
        listView.setAdapter(adapter);

        builder.setCancelable(true);
        builder.setView(row);

        TextView textView = new TextView(TextEditorActivity.this);
        textView.setText("Backup");
        textView.setPadding(20, 30, 20, 30);
        textView.setTextSize(20f);

        builder.setCustomTitle(textView);
        builder.setCustomTitle(textView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(id == 0){
                if(enableSave){
                    onAutoSave(true);
                }
                FirebaseUser user = google.getUser();
                final View view1 = findViewById(R.id.text_edit_layout);

                if(user == null){
                    Util.showSnackBar(view1, "You have to sign in", getResources().getColor(R.color.error));
                    dialog.dismiss();
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(TextEditorActivity.this);
                progressDialog.setTitle("Backup file");
                progressDialog.setMessage("Uploading file....");
                progressDialog.setCancelable(false);
                progressDialog.show();


                try {
                    File file = new File(filePath);
                    Uri upload = Uri.fromFile(file);
                    String name = FileHelper.getName(file);
                    String extension = FileHelper.getExtension(file.toString());
                    mStorageRef = storage.getReference().child(user.getUid()+"/backup/"+name+"."+extension);

                    mStorageRef.putFile(upload)
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            })
                            .addOnSuccessListener(taskSnapshot -> {
                                progressDialog.dismiss();
                                Util.showSnackBar(view1, "Success", getResources().getColor(R.color.success));
                            })
                            .addOnFailureListener(exception -> {
                                progressDialog.dismiss();
                                Util.showSnackBar(view1, "Unable to backup file", getResources().getColor(R.color.error));
                            });


                } catch (Exception e) {
                        e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
    }


    private void shareText(String text){
        if(text.isEmpty()){
            Toast.makeText(TextEditorActivity.this, "Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivityForResult(Intent.createChooser(intent,"share text via:"), Util.SHARE_REQUEST_CODE);
    }

    private void onAutoSave(boolean autoSave){
        if(autoSave){
            // @ if - if the file is new and not save yet validate the filename before saving
            // @ else - overwrite the file
            if(filePath.isEmpty() && !isFileSave){
                File file = FileHelper.validateFileName(FileList.currentDirPath+"/"+title+".txt");
                save(false, file.toString());
            }else{
                save(true, filePath);
            }
        }
    }

    private void translateText(final String text, String sourceLang, String targetLang){
        View  view = findViewById(R.id.text_edit_layout);

        if(sourceLang == "und"){
            Util.showSnackBar(view, "unable to translate language, please check the source language", getResources().getColor(R.color.error));
            return;
        }
        if(sourceLang.equals(targetLang)){
            Util.showSnackBar(view, "please select a different language", getResources().getColor(R.color.error));
            return;
        }

        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLang)
                        .setTargetLanguage(targetLang)
                        .build();
        final Translator translator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(aVoid -> translator.translate(text)
                        .addOnSuccessListener(translatedText -> {
                            editor.setHtml(translatedText);
                            enableSave();

                            Util.showSnackBarUndo(view, "text successfully translated", getResources().getColor(R.color.success),
                                    () -> {
                                        editor.setHtml(text);
                                    });
                        }))
                .addOnFailureListener(e ->
                        Util.showSnackBar(view, "unable to download Model!", getResources().getColor(R.color.error)));
    }

    private void IndetifyLanguage(String text, IFetchContent f){
        LanguageIdentifier languageIdentifier =
                LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        languageCode -> {
                            if (languageCode.equals("und")) {
                                View  view = findViewById(R.id.text_edit_layout);
                                Util.showSnackBar(view, "Unable to detect language", getResources().getColor(R.color.error));
                            } else {
                                f.onFetch(languageCode);
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            View  view = findViewById(R.id.text_edit_layout);
                            Util.showSnackBar(view, "Unable to detect language", getResources().getColor(R.color.error));
                        });
    }

    private void editorUIColor(int bgColor, int fontColor, int toolbarBgColor,
                               int bottomToolbarBgColor, int iconColor){
        editor.setEditorBackgroundColor(bgColor);
        editor.setEditorFontColor(fontColor);
        toolbar.setBackgroundColor(toolbarBgColor);
        menuIconColor = iconColor;
        if(editorToolbar != null){
            editorToolbar.setBackgroundColor(bottomToolbarBgColor);
        }
    }

}