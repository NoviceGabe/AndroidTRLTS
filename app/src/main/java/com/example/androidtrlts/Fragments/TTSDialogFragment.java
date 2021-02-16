package com.example.androidtrlts.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.androidtrlts.Helpers.SessionHelper;
import com.example.androidtrlts.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;

public class TTSDialogFragment extends BottomSheetDialogFragment implements Serializable {
    private String sourceLang;
    private String sourceLangCode;
    private SeekBar pitchSeekBar;
    private SeekBar speedSeekBar;
    private TextToSpeech tts;
    private float ttsPitch;
    private float ttsSpeed;
    private String text;
    private Locale lang;
    private View view;
    private int WRITE_EXTERNAL_STORAGE_CODE = 1;
    private final String googleTtsPackage = "com.google.android.tts";
    private final String picoPackage = "com.svox.pico";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.bottom_sheet_tts_dialog, container, false);

        pitchSeekBar = (SeekBar) view.findViewById(R.id.pitch);
        speedSeekBar = (SeekBar) view.findViewById(R.id.speed);
        final ImageButton button = view.findViewById(R.id.bottom_sheet_dialog_button);

        Bundle bundle = getArguments(); //bundle from TextEditor

        if(bundle != null && (bundle.containsKey("text") && bundle.containsKey("code") && bundle.containsKey("lang"))){
            text = bundle.getString("text");
            sourceLangCode = bundle.getString("code");
            sourceLang = bundle.getString("lang");
        }

        TextToSpeech.OnInitListener listener = status -> {
            if(status == TextToSpeech.SUCCESS){
                //printVoice("cs_CZ");
                //Toast.makeText(getActivity(), "TTS engine has been initialized", Toast.LENGTH_SHORT).show();
                int result = tts.setLanguage(getLocaleByLang(sourceLang));
                if(result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Toast.makeText(getActivity(), "Error: Language not supported",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isPackageInstalled(getActivity().getPackageManager(), googleTtsPackage)){
                    confirmDialog();
                }

                SessionHelper sessionHelper = new SessionHelper(getActivity());
                sessionHelper.setSession("pref_engine", tts.getDefaultEngine());

            }else{
                Toast.makeText(getActivity(), "Error: Initialization failed", Toast.LENGTH_SHORT).show();
            }
        };

        tts = new TextToSpeech(getContext(),listener);

        button.setOnClickListener(view -> speak());


        return view;
    }

    private void confirmDialog(){
        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        d.setTitle("Install recommeded speech engine?");
        d.setMessage("Your device isn't using the recommended speech engine. Do you wish to install it?");
        d.setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int arg1){
                Intent installVoice = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installVoice);
            }});
        d.setNegativeButton("No, later", (dialog, arg1) -> {
            if(isPackageInstalled(getActivity().getPackageManager(), picoPackage))
                tts.setEngineByPackageName(picoPackage);

        });
        d.show();
    }
    public static boolean isPackageInstalled(PackageManager pm, String packageName) {
        try {
            pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public Locale getLocaleByLang(String lang){
        switch (lang.toLowerCase()){
            case "arabic":
                return new Locale("ar_XA");
            case "bangla":
                //tts.setVoice(getVoice("bn-in-x-bnm-local"));
                return new Locale("bn_IN");
            case "czech":
                //tts.setVoice(getVoice("cs-cz-x-jfs-network"));

                return new Locale("cs_CZ");
            case "danish":
                return new Locale("da_DK");
            case "dutch":
                return new Locale("nl_NL");
            case "german":
                return new Locale("de_DE");
            case "greek":
                return new Locale("el_GR");
            case "english":
                return new Locale("en_US");
            case "spanish":
                return new Locale("en_ES");
            case "estonian":
                return new Locale("et_EE");
            case "finnish":
                return new Locale("fi_FI");
            case "filipino":
            case "tagalog":
                return new Locale("fil_PH");
            case "french":
                return new Locale("fr_FR");
            case "hindi":
                return new Locale("hi_IN");
            case "hungarian":
                return new Locale("hu_HU");
            case "indonesian":
                return new Locale("in_ID");
            case "italian":
                return new Locale("it_IT");
            case "japanese":
                return new Locale("ja_JP");
            case "javanese":
                return new Locale("jv_ID");
            case "khmer":
                return new Locale("km_KH");
            case "korean":
                return new Locale("ko_KR");
            case "norwegian bokmål":
                return new Locale("nb_NO");
            case "nepali":
                return new Locale("ne_NP");
            case "polish":
                return new Locale("pl_PL");
            case "portuguese":
                return new Locale("pt_PT");
            case "romanian":
                return new Locale("ro_RO");
            case "russian":
                return new Locale("ru_RU");
            case "sinhala":
                return new Locale("si_LK");
            case "slovak":
                return new Locale("sk_SK");
            case "swedish":
                return new Locale("sv_SE");
            case "tamil":
                return new Locale("ta_IN");
            case "telugu":
                return new Locale("te_IN");
            case "thai":
                return new Locale("th_TH");
            case "turkish":
                return new Locale("tr_TH");
            case "ukranian":
                return new Locale("uk_UA");
            case "vietnamese":
                return new Locale("vi_VN");
            case "cantonese":
                return new Locale("yue_HK_#Hant");
            case "chinese":
                return new Locale("zh_CN_#Hans");
            case "taiwanese":
                return new Locale("zh_TW_#Hant");
            default:
                return null;
        }

    }

    private Locale getLocaleByName(String lang){
        switch (lang){
            case "Bangla (Bangladesh)":
                return new Locale("bn_BD");
            case "Bangla (India)":
                return new Locale("bn_IN");
            case "Czech (Czechia)":
                return new Locale("cs_CZ");
            case "Danish (Denmark)":
                return new Locale("da_DK");
            case "German (Germany)":
                return new Locale("de_DE");
            case "Greek (Greece)":
                return new Locale("el_GR");
            case "English (Australia)":
                return new Locale("en_AU");
            case "English (United Kingdom)":
                return new Locale("en_GB");
            case "English (India)":
                return new Locale("en_IN");
            case "English (Nigeria)":
                return new Locale("en_NG");
            case "English (United States)":
                return new Locale("en_US");
            case "English (United States, Computer)":
                return new Locale("en_US_POSIX");
            case "Spanish (Spain)":
                return new Locale("en_ES");
            case "Spanish (United States)":
                return new Locale("es_US");
            case "Estonian (Estonia)":
                return new Locale("et_EE");
            case "Finnish (Finland)":
                return new Locale("fi_FI");
            case "Filipino (Philippines)":
                return new Locale("fil_PH");
            case "French (Canada)":
                return new Locale("fr_CA");
            case "French (France)":
                return new Locale("fr_FR");
            case "Hindi (India)":
                return new Locale("hi_IN");
            case "Hungarian (Hungary)":
                return new Locale("hu_HU");
            case "Indonesian (Indonesia)":
                return new Locale("in_ID");
            case "Italian (Italy)":
                return new Locale("it_IT");
            case "Japanese (Japan)":
                return new Locale("ja_JP");
            case "Javanese (Indonesia)":
                return new Locale("jv_ID");
            case "Khmer (Cambodia)":
                return new Locale("km_KH");
            case "Korean (South Korea)":
                return new Locale("ko_KR");
            case "Norwegian Bokmål (Norway)":
                return new Locale("nb_NO");
            case "Nepali (Nepal)":
                return new Locale("ne_NP");
            case "Dutch (Netherlands)":
                return new Locale("nl_NL");
            case "Polish (Poland)":
                return new Locale("pl_PL");
            case "Portuguese (Brazil)":
                return new Locale("pt_BR");
            case "Portuguese (Portugal)":
                return new Locale("pt_PT");
            case "Romanian (Romania)":
                return new Locale("ro_RO");
            case "Russian (Russia)":
                return new Locale("ru_RU");
            case "Sinhala (Sri Lanka)":
                return new Locale("si_LK");
            case "Slovak (Slovakia)":
                return new Locale("sk_SK");
            case "Swedish (Sweden)":
                return new Locale("sv_SE");
            case "Thai (Thailand)":
                return new Locale("th_TH");
            case "Turkish (Turkey)":
                return new Locale("tr_TH");
            case "Ukranian (Ukraine)":
                return new Locale("uk_UA");
            case "Vietnamese (Vietnam)":
                return new Locale("vi_VN");
            case "Cantonese (Traditional Han, Hong Kong)":
                return new Locale("yue_HK_#Hant");
            case "Chinese (Simplified Han, China)":
                return new Locale("zh_CN_#Hans");
            case "Chinese (Traditional Han, Taiwan)":
                return new Locale("zh_TW_#Hant");
            default:
                return null;
        }
    }

    private void speak(){
        ttsPitch = (float) pitchSeekBar.getProgress() / 50;
        ttsSpeed = (float) speedSeekBar.getProgress() / 50;
        if(ttsPitch < 0.1){
            ttsPitch = 0.1f;
        }
        if(ttsSpeed < 0.1){
            ttsSpeed = 0.1f;
        }

        //tts.setVoice(getVoice("en-us-x-sfg#male_1-local"));
        tts.setPitch(ttsPitch);
        tts.setSpeechRate(ttsSpeed);
        if(text != null){
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private Voice getVoice(String voice){
        for(Voice tmpVoice : tts.getVoices()){
            if(tmpVoice.getName().equals(voice)){
                return tmpVoice;
            }
        }
        return null;
    }

    @Override
    public void onDestroy() {
        if(tts != null){
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void printVoice(String voice){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/voice list.txt");
        FileWriter fw = null; //create the file
        try {
            fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            for(Voice tmpVoice : tts.getVoices()){
                String locale = tmpVoice.toString().substring(tmpVoice.toString().indexOf("locale:")+7, tmpVoice.toString().indexOf("quality:")-2);
                if(locale.trim().equals(voice)){
                    bw.write(tmpVoice.toString()+"\n\n\n"); //write text to file
                }

            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

