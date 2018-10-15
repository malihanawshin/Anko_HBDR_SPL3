package com.example.imm.anko;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    protected LinearLayout takePhoto;
    protected LinearLayout drawDigit;
    protected LinearLayout choosePhoto;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SELECT_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.take_photo).setOnClickListener(clickListener);
        findViewById(R.id.draw_digit).setOnClickListener(clickListener);
        findViewById(R.id.choose_photo).setOnClickListener(clickListener);

    }


    protected View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

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
        }
    };

    protected void takePhoto(){

        dispatchTakePictureIntent();
        //startActivity(new Intent(BottomBarActivity.this,SettingsActivity.class));
        //finish();

    }

    protected void drawDigit(){
        startActivity(new Intent(MainActivity.this,DrawDigitActivity.class));
        //finish();
    }

    protected void choosePhoto(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"),SELECT_IMAGE);
    }


    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)  {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }
    }
*/

}
