package com.example.imm.anko;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    protected LinearLayout takePhoto;
    protected LinearLayout drawDigit;
    protected LinearLayout choosePhoto;

    static final int request_image_capture = 1;
    static final int select_image = 1;

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

    protected void takePhoto(){

        dispatchTakePictureIntent();
    }

    protected void drawDigit(){
        startActivity(new Intent(MainActivity.this,DrawDigitActivity.class));
        //finish();
    }

    protected void choosePhoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"),select_image);
    }


    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent,request_image_capture);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == select_image) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    detectFromImage(data.getData());
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void detectFromImage(Uri uri) {

        Intent intent = new Intent(MainActivity.this,DetectFromImageActivity.class);
        intent.putExtra("URI",uri);
        startActivity(intent);
    }
}
