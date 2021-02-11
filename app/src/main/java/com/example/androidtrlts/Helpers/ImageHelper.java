package com.example.androidtrlts.Helpers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.Util;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.androidtrlts.Utils.Util.REQUEST_IMAGE_CAPTURE;

public class ImageHelper {
    private Activity activity;
    private int imgWidth, imgHeight;

    public ImageHelper(Activity activity){
        this.activity = activity;
    }

    //for taking photo
    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void startCrop(@NonNull Uri uri){
        Log.d("id",activity.toString());
        String destinationFileName = Util.SAMPLE_CROPPED_IMG_NAME;
        destinationFileName +=".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(activity.getCacheDir(),destinationFileName)));
        //uCrop.withAspectRatio(1,1);
        //uCrop.withAspectRatio(3,4);
        uCrop.useSourceImageAspectRatio();
        //uCrop.withAspectRatio(2,3);
        //uCrop.withAspectRatio(16, 9);
        uCrop.withMaxResultSize(720, 1280);
        uCrop.withOptions(getCropOptions());
        uCrop.start(activity);
    }

    private UCrop.Options getCropOptions(){
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(100);
        //CompressType
        //options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        //UI
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);

        //Colors
        options.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimary));
        options.setToolbarColor(activity.getResources().getColor((R.color.colorPrimary)));

        options.setToolbarTitle("Edit Image");
        return options;
    }

    private Bitmap resizeImg(Uri uri, final int reqSize) throws FileNotFoundException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri), null, opt);

        int width_tmp = opt.outWidth, height_tmp = opt.outHeight;
        int scale = 1;

        while(true){
            if(width_tmp / 2 < reqSize || height_tmp / 2 < reqSize){
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options opt2 = new BitmapFactory.Options();
        opt2.inSampleSize = scale;

        return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri), null, opt2);
    }

    private void getImageSize(Uri uri) throws IOException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri), null, opt);
        this.imgWidth = opt.outWidth;
        this.imgHeight = opt.outHeight;
        activity.getContentResolver().openInputStream(uri).close();
    }

    public Uri getUriFromBitmap(Bitmap bitmap){
        String timeStamp = new SimpleDateFormat("ddMMyyyyHHmmss").format(new Date());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage( this.activity.getContentResolver(), bitmap, timeStamp, null);
        return Uri.parse(path);
    }

    public Bitmap getBitmapFromUri(Uri uri){
        InputStream imageStream = null;

        try {
            imageStream =  activity.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(imageStream);
    }

    private int getImageHeight(){
        return imgHeight;
    }

    private int getImageWidth(){
        return imgWidth;
    }
}
