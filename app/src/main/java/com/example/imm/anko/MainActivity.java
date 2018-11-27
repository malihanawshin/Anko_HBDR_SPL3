package com.example.imm.anko;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    protected LinearLayout takePhoto;
    protected LinearLayout drawDigit;
    protected LinearLayout choosePhoto;
    private static final String TAG = "Ongko";

    private IO io = new IO();
    ArrayList<String> mPhotoNames = new ArrayList<String>(50);
    private final int ACTIVITY_START_CAMERA_APP = 0;
    int mPhotoNum = 0;
    private String mImageFileLocation;
    private ImageProcessor ip;

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int SELECT_IMAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.take_photo).setOnClickListener(clickListener);
        findViewById(R.id.draw_digit).setOnClickListener(clickListener);
        findViewById(R.id.choose_photo).setOnClickListener(clickListener);

    }


    protected View.OnClickListener clickListener = v -> {

        switch (v.getId()) {
            case R.id.take_photo:
                takePhoto();
                break;

            case R.id.draw_digit:
                drawDigit();
                break;

            case R.id.choose_photo:
                choosePhoto();
                break;


        }
    };

    protected void takePhoto() {
        dispatchTakePictureIntent();
    }

    protected void drawDigit() {
        startActivity(new Intent(MainActivity.this, DrawDigitActivity.class));
    }

    protected void choosePhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), SELECT_IMAGE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Intent callCameraApplicationIntent = new Intent();
        //callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        photoFile = io.createImageFile("photo.jpg");
        mImageFileLocation = photoFile.getAbsolutePath();
        mPhotoNames.add(photoFile.getName());
        Log.d(TAG, "Image: " + photoFile.getName());
        //callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        /*callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                photoFile));*/
            //startActivityForResult(callCameraApplicationIntent, request_image_capture);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    //detectFromImage(data.getData());
                    detectFromCapturedImage(data.getData());
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                //Uri uri = io.saveImage(this, (Bitmap)data.getExtras().getData("data"));
                //detectFromCapturedImage(data.getData());
            }
         else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Cancelled camera", Toast.LENGTH_SHORT).show();
        }
    }

    }

    private void detectFromImage(Uri uri) {
        Intent intent = new Intent(MainActivity.this,DetectFromImageActivity.class);
        intent.putExtra("URI",uri);
        startActivity(intent);
    }

    private void detectFromCapturedImage(Uri uri) {
        Bitmap bitmap = io.getCameraPhoto(getFileName(uri));
        Bitmap origImage = io.getCameraPhoto(getFileName(uri));

        //Preproces the image to remove noise, amplify the region of interest with the number
        //by making it bright white and make the background completely black.
        Mat imgToProcess = ip.preProcessImage(bitmap);

        //Scale down the bitmap based on the processing height and width (640 x 480)
        Bitmap.createScaledBitmap(bitmap,imgToProcess.width(),imgToProcess.height(),false);
        Bitmap.createScaledBitmap(origImage,imgToProcess.width(),imgToProcess.height(),false);

        //Convert to bitmap and save the photo to display in the app.
        Utils.matToBitmap(imgToProcess.clone(),bitmap);
        savePhoto(bitmap,"photo_preprocess.jpg");

        //Pass the preprocessed image to perform segmentation and recogntion. This also
        //overlays rectangular boxes on the segmented digits.
        //Mat boundImage = ip.segmentAndRecognize(imgToProcess,origImage,mDetector);
        Mat boundImage = ip.segmentAndRecognize(imgToProcess,origImage);
        Utils.matToBitmap(boundImage.clone(),bitmap);
        if(isStoragePermissionGranted()) {
            savePhoto(bitmap, "photo_bound.jpg");
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void savePhoto(Bitmap bm, String photoName) {
        mPhotoNames.add(photoName);
        io.saveImage(bm,photoName);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

}
