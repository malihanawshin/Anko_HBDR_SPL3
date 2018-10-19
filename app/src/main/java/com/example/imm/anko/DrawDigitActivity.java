package com.example.imm.anko;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class DrawDigitActivity extends AppCompatActivity {

    protected DrawView drawview;
    protected ImageView canvas;

    private static final int width = 32;
    private static final int height = 32;
    private static final String model_path = "file:///android_asset/opt_bangla_digit_convnet_v2.pb";
    private static final String label_path = "file:///android_asset/labels.txt";
    private static final int input_size = 32;
    private static final String input_name = "conv2d_1_input";
    private static final String output_name = "activation_2/Softmax";

    private Executor executor = Executors.newSingleThreadExecutor();
    private DigitClassifier digitClassifier;

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

        Bitmap originalImage = drawview.getBitmap();
        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage, 32, 32, false);
        canvas.setImageBitmap(scaledImage);
        changeVisibility();

        int pixels[] = new int[height*width];
        scaledImage.getPixels(pixels,0,height, 0,0,width,height);
        float[] normalizedPixels = normalizePixels(pixels);
        int[] previewPixels = pixelsForPreview(pixels, normalizedPixels);

        Bitmap preview = Bitmap.createBitmap(previewPixels, width, height, Bitmap.Config.ARGB_8888);
        canvas.setImageBitmap(preview);

        classify(normalizedPixels);

    }

    private void classify(float[] normalizedPixels) {
        DigitClassification dc = digitClassifier.recognize(normalizedPixels);
        String result = String.format("Digit %s with confidence: %f",dc.getLabel(),dc.getConfidence());
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
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

        executor.execute(() -> {
            try {
                digitClassifier = DigitClassifier.create(getApplicationContext().getAssets(),
                        model_path, label_path, input_size, input_name, output_name);
            } catch (final Exception e) {
                throw new RuntimeException("Error while initializing TensorFlow!", e);
            }
        });
    }

    private float[] normalizePixels(int[] pixels){
        float[] normalizedPixels = ConvertColor.convertForTensorflow(pixels);
        return normalizedPixels;
    }

    private int[] pixelsForPreview(int[] pixels, float[] normalizedPixels) {
        int[] previewPixels = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            previewPixels[i] = ConvertColor.convertTfToPixel(normalizedPixels[i]);
        }
        return previewPixels;
    }


}
