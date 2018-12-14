package com.example.imm.anko;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.imm.anko.ImageUtils.getResizedBitmap;


public class DrawDigitActivity extends AppCompatActivity {

    private static final int width = 32;
    private static final int height = 32;
    private static final String model_path = "file:///android_asset/opt_bangla_digit_convnet_v2.pb";
    private static final String label_path = "file:///android_asset/labels.txt";
    private static final int input_size = 32;
    private static final String input_name = "conv2d_1_input";
    private static final String output_name = "activation_2/Softmax";

    private Executor executor = Executors.newSingleThreadExecutor();
    private DigitClassifier digitClassifier;
    private ImageProcessor ip = new ImageProcessor();
    private ImageUtils io = new ImageUtils();
    Bitmap originalImage;
    private int number;
    ArrayList<Integer> numbers = new ArrayList<Integer>(50);
    private String CURRENT_ACTION = null;

    protected DrawView drawview;
    protected ImageView canvas;
    protected TextView text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_in_canvas);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawview = findViewById(R.id.drawView);
        canvas = findViewById(R.id.canvasImageview);
        text = findViewById(R.id.result);

        findViewById(R.id.recognizeButton).setOnClickListener(clickListener);
        findViewById(R.id.resetButton).setOnClickListener(clickListener);
        //findViewById(R.id.mul).setOnClickListener(clickListener);
        //findViewById(R.id.equal).setOnClickListener(clickListener);

        load();
        if(OpenCVLoader.initDebug()){
            Log.e("OpenCv", "Loaded!");
        }
        else {
            Log.e("OpenCv", "Could not load!");
        }

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
                case R.id.mul:
                     onReset();
                     //calculate("*",number);
                     CURRENT_ACTION = "*";
                     break;
                case R.id.equal:
                     onRecognize();
                     calculate(numbers);
                     break;
            }
        }
    };

    private void calculate(ArrayList<Integer> arr) {

        int ans=1;

        for(int i=0;i<arr.size();i++){
            if(CURRENT_ACTION=="*") ans = arr.get(i) * arr.get(i+1);
            arr.add(ans);
        }
        text.setText(ans);
    }


    private void onRecognize() {

        originalImage = drawview.getBitmap();

        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage, 32, 32, false);

        /*Bitmap originalImage = getResizedBitmap(originalImage,2500,2500);
        Mat imgMat = ip.preProcessImage(bitmap);
        Utils.matToBitmap(imgMat,bitmap);
        savePhoto(bitmap, "photo_preprocess.jpg");

        Mat boundImage = ip.segmentAndRecognize(imgMat,scaledImage,digitClassifier);
        Utils.matToBitmap(boundImage.clone(), bitmap);
        savePhoto(bitmap, "photo_bound.jpg");
*/
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

    public void savePhoto(Bitmap bm, String photoName) {
        io.saveImage(bm, photoName);
    }

    private void classify(float[] normalizedPixels) {
        DigitClassification dc = digitClassifier.recognize(normalizedPixels);
        NumberToWord nw = new NumberToWord();
        number = Integer.parseInt(dc.getLabel());
        numbers.add(number);
        String num = nw.numberToWords(number);
        String result = String.format("%s",num);
        //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        text.setText(result);
    }

    private void changeVisibility() {
        canvas.setVisibility(View.VISIBLE);
        drawview.setVisibility(View.GONE);
    }


    private void onReset() {
        drawview.reset();
        canvas.setVisibility(View.GONE);
        text.setText("");
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

    public static float[] normalizePixels(int[] pixels){
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

    public void multiplication(){

    }

}
