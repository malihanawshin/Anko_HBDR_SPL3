package com.example.imm.anko;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.THRESH_OTSU;
import static org.opencv.imgproc.Imgproc.threshold;


public class DetectFromImageActivity extends AppCompatActivity {

    protected ImageView image;
    protected Bitmap bitmap;
/*
    private static final int width = 32;
    private static final int height = 32;
    private static final String model_path = "file:///android_asset/opt_bangla_digit_convnet_v2.pb";
    private static final String label_path = "file:///android_asset/labels.txt";
    private static final int input_size = 32;
    private static final String input_name = "conv2d_1_input";
    private static final String output_name = "activation_2/Softmax";

    private Executor executor = Executors.newSingleThreadExecutor();
    private DigitClassifier digitClassifier;
*/
    private Uri uri;
    public ImageProcessor ip = new ImageProcessor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_from_image);

        Intent intent = getIntent();
        uri = intent.getParcelableExtra("URI");
        image = findViewById(R.id.imageOfDigit);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        image.setImageBitmap(bitmap);

        //findViewById(R.id.recognizeButton).setOnClickListener(clickListener);
        //findViewById(R.id.resetButton).setOnClickListener(clickListener);

        findViewById(R.id.recognizeButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.resetButton).setVisibility(View.INVISIBLE);



/*
        if(OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "Loaded", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
        }

        load();
*/
    }


    protected View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.recognizeButton:
                    //onRecognize();
                    break;
                case R.id.resetButton:
                    //onReset();
                    break;
            }
        }
    };


/*
    private void onRecognize() {

        //bitmap = convertToGray(bitmap);
        //bitmap = convert(bitmap);
        Bitmap scaledImage = Bitmap.createScaledBitmap(bitmap, 32, 32, false);
        scaledImage = ConvertColor.convertImage(scaledImage);

        image.setImageBitmap(scaledImage);

        int pixels[] = new int[height*width];
        scaledImage.getPixels(pixels,0,height, 0,0,width,height);

        float[] normalizedPixels = normalizePixels(pixels);
        //float[] normalizedPixels = ConvertColor.convertNew(pixels);
        int[] previewPixels = pixelsForPreview(pixels, normalizedPixels);

        Bitmap preview = Bitmap.createBitmap(previewPixels, width, height, Bitmap.Config.ARGB_8888);
        image.setImageBitmap(preview);

        classify(normalizedPixels);
    }
*/

/*
    private Bitmap convertToGray(Bitmap bitmap) {

        //opencv_core.IplImage image = cvLoadImage(uri.getPath(),0);
        Bitmap grayBitmap;

        Mat rgbaMat = new Mat();
        Mat grayMat = new Mat();
        Mat detectedEdges = new Mat();
        Mat dest = new Mat();

        Utils.bitmapToMat(bitmap,rgbaMat);

        Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(grayMat,detectedEdges, new Size(3,3));
        Imgproc.Canny(detectedEdges, detectedEdges, 10, 10*3,3,false);

        Core.add(dest, Scalar.all(0), dest);
        rgbaMat.copyTo(dest,detectedEdges);

        grayBitmap = Bitmap.createBitmap(rgbaMat.cols(), rgbaMat.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(rgbaMat,grayBitmap);

        return  grayBitmap;
    }

    private Bitmap convert(Bitmap grayBitmap) {

        //Mat rgbaMat = new Mat(32,32,CV_8UC1);
        Mat grayMat = new Mat();
        Mat resizedMat = new Mat(32,32,CV_8UC1);
        Mat gaussianMat = new Mat(32,32,CV_8UC1);
        Mat weightedMat = new Mat(32,32,CV_8UC1);
        Mat filteredMat = new Mat(32,32,CV_8UC1);
        Mat threshedMat = new Mat(32,32,CV_8UC1);

        int kernelsize = 3;

        Bitmap testBitmap = Bitmap.createBitmap(32,32,Bitmap.Config.RGB_565);
        Bitmap finalBitmap = Bitmap.createBitmap(32,32,Bitmap.Config.RGB_565);

        Utils.bitmapToMat(grayBitmap,grayMat);
        //Imgproc.cvtColor(rgbaMat,grayMat,Imgproc.COLOR_RGB2GRAY);
        //Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_GRAY2RGBA, 4);
        */
/*for(int i=0;i<rgbaMat.height();i++){
            for(int j=0;j<rgbaMat.width();j++){
                double y = 0.3 * rgbaMat.get(i, j)[0] + 0.59 * rgbaMat.get(i, j)[1] + 0.11 * rgbaMat.get(i, j)[2];
                rgbaMat.put(i, j, new double[]{y, y, y, 255});
            }
        }
*//*


        //Utils.matToBitmap(grayMat,testBitmap);

        Imgproc.resize(grayMat,resizedMat,new Size(32,32),0,0,INTER_AREA);
        Imgproc.GaussianBlur(resizedMat,gaussianMat,new Size(9,9),10.0);

        if(!gaussianMat.empty() && gaussianMat.type() == CV_8UC1) {
            Imgproc.accumulateWeighted(gaussianMat, weightedMat, 1.5); //error: (-215) func != 0
            //in function void cv::accumulateWeighted
        }
        Mat kernel = new Mat(kernelsize,kernelsize, CV_8UC1){
            {
                put(-1,-1,-1);
                put(-1,9,-1);
                put(-1,-1,-1);

            }
        };
        //Imgproc.filter2D(weightedMat,filteredMat,-1,kernel);

        threshold(weightedMat, threshedMat, 128,255,THRESH_BINARY_INV+THRESH_OTSU);
        //threshold(gaussianMat, threshedMat, 128,255,THRESH_BINARY_INV+THRESH_OTSU);
        //threshold(resizedMat, threshedMat, 128,255,THRESH_BINARY_INV+THRESH_OTSU);
        //threshold(grayMat, threshedMat, 128,255,THRESH_BINARY_INV+THRESH_OTSU);
        //threshold(grayMat, threshedMat, 128,255,THRESH_BINARY_INV);
        //threshold(grayMat, threshedMat, 128,255,THRESH_BINARY);

        //Utils.matToBitmap(threshedMat,finalBitmap);
        Utils.matToBitmap(threshedMat,finalBitmap);

        return finalBitmap;

    }

    private void classify(float[] normalizedPixels) {
        DigitClassification dc = digitClassifier.recognize(normalizedPixels);
        NumberToWord nw = new NumberToWord();
        int number = Integer.parseInt(dc.getLabel());
        String num = nw.numberToWords(number);
        String result = String.format("Digit: %s, %s",dc.getLabel(),num);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }


    private void onReset() {

    }

    private float[] normalizePixels(int[] pixels){
        //int[] p = ConvertColor.convert(pixels);
        //Bitmap preview = Bitmap.createBitmap(p, width, height, Bitmap.Config.ARGB_8888);

        float[] normalizedPixels = ConvertColor.convertForTensorflow(pixels);
        */
/*float[] normalizedPixels = new float[height*width];
        for(int i=0;i<pixels.length;i++){
            normalizedPixels[i] = (float) pixels[i];
        }*//*

        return normalizedPixels;
    }

    private int[] pixelsForPreview(int[] pixels, float[] normalizedPixels) {
        int[] previewPixels = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            previewPixels[i] = ConvertColor.convertTfToPixel(normalizedPixels[i]);
        }
        return previewPixels;
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
*/
}
