package com.example.androidtrlts.Helpers;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.androidtrlts.Activities.MainActivity;
import com.example.androidtrlts.Activities.TextEditorActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

public class OCRHelper {

    public static Uri imageUriResultCrop;
    public  static int tries = 0;

    private static void processTextRecognitionResult(Activity activity, Text texts){
        String resultText = texts.getText();
        tries++;

        if(resultText.isEmpty()){
            if(tries >= 3 && tries < 6){
                Toast.makeText(activity,
                        "Try to switch to on-cloud text recognition to recognize the text with high accuracy. " +
                                "Go to the main settings.",
                        Toast.LENGTH_SHORT).show();
            }else if(tries >= 6 && tries < 10){
                Toast.makeText(activity,
                        "For better result make sure that the image is in good quality.",
                        Toast.LENGTH_SHORT).show();
            }else if(tries == 10){
                Toast.makeText(activity,
                        "\"Insanity is doing the same thing over and over again and expecting different results.\" - Albert Einstein",
                        Toast.LENGTH_SHORT).show();
                tries = 0;
            }else{
                Toast.makeText(activity, "No text found or cannot read image", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        tries = 0;
        int counter = 0;
        String title = "";
        for(Text.TextBlock block:texts.getTextBlocks()){
            String txt = block.getText();
            resultText.concat(txt);//concatentate text

            if(counter == 0){
                if(block.getText().contains("\n")){
                    title = block.getText().substring(0, block.getText().indexOf("\n"));
                }else{
                    title = block.getText();
                }
                counter++;
            }
        }
        // intent bundle resulttext
        Intent intent = new Intent(activity, TextEditorActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("extractedText", resultText);
        bundle.putString("title", title);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        //Toast.makeText(activity, "ok", Toast.LENGTH_SHORT).show();
    }

    public static void runTextRecognition(Activity activity) {
        ImageHelper imageHelper = new ImageHelper(activity);
        Bitmap imageBitmap = null;

        if(imageUriResultCrop != null){
            imageBitmap = imageHelper.getBitmapFromUri(imageUriResultCrop);
        }

        if(imageBitmap == null){
            Toast.makeText(activity, "Error: Image Bitmap doesn't exist", Toast.LENGTH_SHORT).show();
            return;
        }
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);

        TextRecognizer recognizer = getTextRecognizer();
        recognizer.process(image)
                .addOnSuccessListener(
                        texts -> {
                            processTextRecognitionResult(activity,texts);
                        })
                .addOnFailureListener(
                        e -> {
                            e.printStackTrace();
                        });
    }

    public static  void runOnCloudTextRecognition(Activity activity){
        ImageHelper imageHelper = new ImageHelper(activity);
        Bitmap imageBitmap = null;

        if(imageUriResultCrop != null){
            imageBitmap = imageHelper.getBitmapFromUri(imageUriResultCrop);
        }

        if(imageBitmap == null){
            Toast.makeText(activity, "Error: Image Bitmap doesn't exist", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseVisionImage  image = FirebaseVisionImage.fromBitmap(imageBitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudTextRecognizer();

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener((OnSuccessListener<FirebaseVisionText>) firebaseVisionText ->

                                processTextRecognitionResult(activity,firebaseVisionText))
                        .addOnFailureListener(
                                (OnFailureListener) e -> e.printStackTrace());
    }

    private static void processTextRecognitionResult(Activity activity, FirebaseVisionText texts){
        String resultText = texts.getText();

        if(resultText.isEmpty()){
            Toast.makeText(activity, "No text found or cannot read image", Toast.LENGTH_SHORT).show();
            return;
        }

        int counter = 0;
        String title = "";
        for(FirebaseVisionText.TextBlock block:texts.getTextBlocks()){
            String txt = block.getText();
            resultText.concat(txt);//concatentate text

            if(counter == 0){
                if(block.getText().contains("\n")){
                    title = block.getText().substring(0, block.getText().indexOf("\n"));
                }else{
                    title = block.getText();
                }
                counter++;
            }
        }
        // intent bundle resulttext
        Intent intent = new Intent(activity, TextEditorActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("extractedText", resultText);
        bundle.putString("title", title);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        //Toast.makeText(activity, "ok", Toast.LENGTH_SHORT).show();
    }

    private static TextRecognizer getTextRecognizer() {
        return TextRecognition.getClient();
    }

}
