package com.example.imm.anko;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.File;
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

    boolean gotFirstNumber =  false;
    long calculation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_in_canvas);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawview = findViewById(R.id.drawView);
        canvas = findViewById(R.id.canvasImageview);
        text = findViewById(R.id.result);

        findViewById(R.id.recognizeButton).setOnClickListener(clickListener);
        findViewById(R.id.recognizeNum).setOnClickListener(clickListener);
        findViewById(R.id.resetButton).setOnClickListener(clickListener);
        findViewById(R.id.plus).setOnClickListener(clickListener);
        findViewById(R.id.minus).setOnClickListener(clickListener);
        findViewById(R.id.mul).setOnClickListener(clickListener);
        findViewById(R.id.div).setOnClickListener(clickListener);
        findViewById(R.id.equal).setOnClickListener(clickListener);

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
                     onRecognizeOk();
                     break;
                case R.id.recognizeNum:
                     onRecognize();
                     break;
                case R.id.resetButton:
                     onReset();
                     break;
                case R.id.plus:
                     onReset();
                     CURRENT_ACTION = "+";
                     break;
                case R.id.minus:
                    onReset();
                    CURRENT_ACTION = "-";
                    break;
                case R.id.mul:
                    onReset();
                    CURRENT_ACTION = "*";
                    break;
                case R.id.div:
                    onReset();
                    CURRENT_ACTION = "/";
                    break;
                /*case R.id.equal:
                     onRecognize();
                     calculate(number);
                     break;*/
            }
        }
    };

    private void calculate(int number) {

        if (CURRENT_ACTION == "+") calculation = calculation + number;
        if (CURRENT_ACTION == "-") calculation = calculation - number;
        if (CURRENT_ACTION == "*") calculation = calculation * number;
        if (CURRENT_ACTION == "/" && number!=0) calculation = calculation / number;

        text.setText(Long.toString(calculation));
        CURRENT_ACTION = null;
    }

    private void onRecognizeOk() {

        originalImage = drawview.getBitmap();

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

    private void onRecognize() {

        originalImage = drawview.getBitmap();

        String path = io.savePicture(this,originalImage);
        detectFromCanvasImage(path);

        String digit = ip.getResultNum();
        String word = ip.getResultText();

        Bitmap scaledImage = Bitmap.createScaledBitmap(originalImage, 32, 32, false);
        canvas.setImageBitmap(scaledImage);
        changeVisibility();

        int pixels[] = new int[height*width];
        scaledImage.getPixels(pixels,0,height, 0,0,width,height);
        float[] normalizedPixels = normalizePixels(pixels);
        int[] previewPixels = pixelsForPreview(pixels, normalizedPixels);

        Bitmap preview = Bitmap.createBitmap(previewPixels, width, height, Bitmap.Config.ARGB_8888);
        canvas.setImageBitmap(preview);
        text.setText(word);
    }

    private void detectFromCanvasImage(String path) {

        Bitmap bitmap = io.getCameraPhoto(path);
        Bitmap origImage = io.getCameraPhoto(path);
        Mat mat = new Mat();

        Mat imgToProcess = ip.preProcessImage(bitmap);

        Bitmap.createScaledBitmap(bitmap, imgToProcess.width(), imgToProcess.height(), false);
        Bitmap.createScaledBitmap(origImage, imgToProcess.width(), imgToProcess.height(), false);

        Utils.matToBitmap(imgToProcess.clone(), bitmap);
        savePhoto(bitmap, "photo_preprocessed.jpg");
        Mat boundImage = ip.segmentAndRecognize(imgToProcess, origImage, digitClassifier);

        Utils.matToBitmap(boundImage.clone(), bitmap);
        savePhoto(bitmap, "photo_bound.jpg");
    }


    public File savePhoto(Bitmap bm, String photoName) {
        File photo = io.saveImage(bm, photoName);
        return photo;
    }

    private void classify(float[] normalizedPixels) {
        DigitClassification dc = digitClassifier.recognize(normalizedPixels);
        NumberToWord nw = new NumberToWord();
        number = Integer.parseInt(dc.getLabel());
        //numbers.add(number);
        String num = nw.numberToWords(number);
        String result = String.format("%s",num);
        text.setText(result);

        if(gotFirstNumber==false) gotFirstNumber=true;

        if (gotFirstNumber && CURRENT_ACTION != null) {
            calculate(number);
        }
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

}
