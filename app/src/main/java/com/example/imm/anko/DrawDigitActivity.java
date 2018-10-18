package com.example.imm.anko;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


public class DrawDigitActivity extends AppCompatActivity {

    protected DrawView drawview;
    protected ImageView canvas;

    private static final int width = 32;
    private static final int height = 32;
    private static final String model_file = "file:///android_asset/opt_bangla_digit_convnet_v2.pb";
    private static final String label_file = "file:///android_asset/labels.txt";
    private static final int input_size = 32;
    private static final String input_name = "conv2d_1_input";
    private static final String output_name = "activation_2/Softmax";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_in_canvas);

        drawview = findViewById(R.id.drawView);
        canvas = findViewById(R.id.canvasImageview);

        findViewById(R.id.recognizeButton).setOnClickListener(clickListener);
        findViewById(R.id.resetButton).setOnClickListener(clickListener);

        load();
    }

    protected View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.recognizeButton:
                     onRecognize();
                     break;
                case R.id.resetButton:
                     onReset();
                     break;
            }
        }
    };

    private void onRecognize() {

        Bitmap original = drawview.getBitmap();
        Bitmap scaled = Bitmap.createScaledBitmap(original, 32, 32, false);
        canvas.setImageBitmap(scaled);
        changeVisibility();



    }

    private void changeVisibility() {
        canvas.setVisibility(View.VISIBLE);
        drawview.setVisibility(View.GONE);
    }


    private void onReset() {
        drawview.reset();
        canvas.setVisibility(View.GONE);
        drawview.setVisibility(View.VISIBLE);
    }

    private void load() {


    }


}
