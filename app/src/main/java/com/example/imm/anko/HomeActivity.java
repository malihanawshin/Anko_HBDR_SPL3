package com.example.imm.anko;

import android.Manifest;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "Ongko";

    private ImageUtils io = new ImageUtils();
    ArrayList<String> mPhotoNames = new ArrayList<String>(50);
    private final int ACTIVITY_START_CAMERA_APP = 0;
    int mPhotoNum = 0;
    private String mImageFileLocation;
    private ImageProcessor ip = new ImageProcessor();

    private static final int width = 32;
    private static final int height = 32;
    private static final String model_path = "file:///android_asset/opt_bangla_digit_convnet_v2.pb";
    private static final String label_path = "file:///android_asset/labels.txt";
    private static final int input_size = 32;
    private static final String input_name = "conv2d_1_input";
    private static final String output_name = "activation_2/Softmax";

    private Executor executor = Executors.newSingleThreadExecutor();
    private DigitClassifier digitClassifier;
    private DetectFromImageActivity df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.take_photo).setOnClickListener(clickListener);
        findViewById(R.id.draw_digit).setOnClickListener(clickListener);
        findViewById(R.id.choose_photo).setOnClickListener(clickListener);

        if(OpenCVLoader.initDebug()){
            Log.e("OpenCv", "Loaded!");
        }
        else {
            Log.e("OpenCv", "Could not load!");
        }

        load();
    }


    protected View.OnClickListener clickListener = v -> {

        switch (v.getId()) {
            case R.id.take_photo:
                openCameraOrGallery();
                break;

            case R.id.draw_digit:
                drawDigit();
                break;

            case R.id.choose_photo:
                openCameraOrGallery();
                break;


        }
    };

    private void openCameraOrGallery() {
        if (isStoragePermissionGranted()) {
            createChooser();
        }
    }

    private void createChooser() {
        // image gallery or camera, doesn't matter
        CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    protected void drawDigit() {
        startActivity(new Intent(HomeActivity.this, DrawDigitActivity.class));
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri uri = result.getUri();
                String path = getPath(uri);
                File file = new File(path);
                detectFromCapturedImage(path);

                String digit = ip.getResultNum();
                String word = ip.getResultText();

                detectFromImage(uri,digit,word);

                //Toast.makeText(this, ip.getResultNum(), Toast.LENGTH_LONG).show();
                //Toast.makeText(this, ip.getResultText(), Toast.LENGTH_LONG).show();
                if (file.exists()) {
                    Log.e("HomeActivity", "Yeah gotcha!");
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e("HomeActivity", "Could not crop");
            }
        }
    }

    private String getPath(Uri uri) {
        if (uri == null) return null;
        String projection[] = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(index);
            return path;
        }
        return uri.getPath();
    }


    private void detectFromImage(Uri uri,String digit,String word) {
        Intent intent = new Intent(HomeActivity.this, DetectFromImageActivity.class);
        intent.putExtra("URI", uri);
        intent.putExtra("Digit",digit);
        intent.putExtra("Word",word);
        startActivity(intent);
    }

    private void detectFromCapturedImage(String path) {
        Bitmap bitmap = io.getCameraPhoto(path);
        Bitmap origImage = io.getCameraPhoto(path);

        //Preproces the image to remove noise, amplify the region of interest with the number
        //by making it bright white and make the background completely black.
        Mat imgToProcess = ip.preProcessImage(bitmap);

        //Scale down the bitmap based on the processing height and width (640 x 480)
        Bitmap.createScaledBitmap(bitmap, imgToProcess.width(), imgToProcess.height(), false);
        Bitmap.createScaledBitmap(origImage, imgToProcess.width(), imgToProcess.height(), false);

        //Convert to bitmap and save the photo to display in the app.
        Utils.matToBitmap(imgToProcess.clone(), bitmap);
        savePhoto(bitmap, "photo_preprocess.jpg");

        Mat boundImage = ip.segmentAndRecognize(imgToProcess, origImage, digitClassifier);

        Utils.matToBitmap(boundImage.clone(), bitmap);
        savePhoto(bitmap, "photo_bound.jpg");
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
        return uri.getPath();
    }

    public void savePhoto(Bitmap bm, String photoName) {
        mPhotoNames.add(photoName);
        io.saveImage(bm, photoName);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private void load() {

        executor.execute(() -> {
            try {
                digitClassifier = DigitClassifier.create(getApplicationContext().getAssets(),
                        model_path, label_path, input_size, input_name, output_name);
            } catch (final Exception e) {
                throw new RuntimeException("Error while initializing TensorFlow!", e);
            }
        });
    }

}
