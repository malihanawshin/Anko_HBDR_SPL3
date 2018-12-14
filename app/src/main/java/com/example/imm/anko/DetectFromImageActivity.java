package com.example.imm.anko;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
    protected TextView view_digit, view_word;
    private Uri uri;
    private String digit, word;
    public ImageProcessor ip = new ImageProcessor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_from_image);

        Intent intent = getIntent();
        uri = intent.getParcelableExtra("URI");
        digit = intent.getStringExtra("Digit");
        word = intent.getStringExtra("Word");
        image = findViewById(R.id.imageOfDigit);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        image.setImageBitmap(bitmap);

        findViewById(R.id.recognizeButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.resetButton).setVisibility(View.INVISIBLE);
        view_digit = findViewById(R.id.result_num);
        view_word = findViewById(R.id.result_word);

        show(digit, word);
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


    public void show(String digit, String word) {
        view_digit.setText(digit);
        view_word.setText(word);
    }


    private Bitmap convertToGray(Bitmap bitmap) {

        Bitmap grayBitmap;

        Mat rgbaMat = new Mat();
        Mat grayMat = new Mat();
        Mat detectedEdges = new Mat();
        Mat dest = new Mat();

        Utils.bitmapToMat(bitmap, rgbaMat);

        Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(grayMat, detectedEdges, new Size(3, 3));
        Imgproc.Canny(detectedEdges, detectedEdges, 10, 10 * 3, 3, false);

        Core.add(dest, Scalar.all(0), dest);
        rgbaMat.copyTo(dest, detectedEdges);

        grayBitmap = Bitmap.createBitmap(rgbaMat.cols(), rgbaMat.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(rgbaMat, grayBitmap);

        return grayBitmap;
    }

    private Bitmap convert(Bitmap grayBitmap) {

        Mat rgbaMat = new Mat(32,32,CV_8UC1);
        Mat grayMat = new Mat();
        Mat resizedMat = new Mat(32, 32, CV_8UC1);
        Mat gaussianMat = new Mat(32, 32, CV_8UC1);
        Mat weightedMat = new Mat(32, 32, CV_8UC1);
        Mat filteredMat = new Mat(32, 32, CV_8UC1);
        Mat threshedMat = new Mat(32, 32, CV_8UC1);

        int kernelsize = 3;

        Bitmap testBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.RGB_565);
        Bitmap finalBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.RGB_565);

        Utils.bitmapToMat(grayBitmap, grayMat);
        Imgproc.cvtColor(rgbaMat,grayMat,Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_GRAY2RGBA, 4);


        Utils.matToBitmap(grayMat, testBitmap);

        Imgproc.resize(grayMat, resizedMat, new Size(32, 32), 0, 0, INTER_AREA);
        Imgproc.GaussianBlur(resizedMat, gaussianMat, new Size(9, 9), 10.0);

        if (!gaussianMat.empty() && gaussianMat.type() == CV_8UC1) {
            Imgproc.accumulateWeighted(gaussianMat, weightedMat, 1.5); //error: (-215) func != 0

        }
        Mat kernel = new Mat(kernelsize, kernelsize, CV_8UC1) {
            {
                put(-1, -1, -1);
                put(-1, 9, -1);
                put(-1, -1, -1);

            }
        };
        Imgproc.filter2D(weightedMat,filteredMat,-1,kernel);

        threshold(weightedMat, threshedMat, 128, 255, THRESH_BINARY_INV + THRESH_OTSU);
        threshold(gaussianMat, threshedMat, 128,255,THRESH_BINARY_INV+THRESH_OTSU);
        threshold(resizedMat, threshedMat, 128,255,THRESH_BINARY_INV+THRESH_OTSU);
        threshold(grayMat, threshedMat, 128,255,THRESH_BINARY_INV+THRESH_OTSU);
        threshold(grayMat, threshedMat, 128,255,THRESH_BINARY_INV);

        Utils.matToBitmap(threshedMat,finalBitmap);
        Utils.matToBitmap(threshedMat, finalBitmap);

        return finalBitmap;

    }


}